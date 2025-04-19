package com.example.naver.domain.service;

import com.example.naver.domain.dto.MessageQueueRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateListItemRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateListRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateRequestDto;
import com.example.naver.domain.dto.story.req.StoryUpdateRequestDto;
import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.dto.story.res.StoryListItemResponseDto;
import com.example.naver.domain.entity.member.Couple;
import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.entity.story.Story;
import com.example.naver.domain.generator.StoryIDGenerator;
import com.example.naver.domain.redisService.messageQueue.QueueService;
import com.example.naver.domain.redisService.story.DeletedStoryCacheService;
import com.example.naver.domain.redisService.story.StoryCacheService;
import com.example.naver.domain.redisService.story.UpdatedStoryCacheService;
import com.example.naver.domain.repository.StoryRepository;
import com.example.naver.web.exception.story.StoryException;
import com.example.naver.web.security.CryptoUtil;
import com.example.naver.web.security.EncryptorConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class StoryService {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final StoryIDGenerator IDGenerator;
    private final DeletedStoryCacheService deletedStoryCacheService;
    private final StoryRepository storyRepository;
    private final CoupleLockService coupleLockService;
    private final JdbcTemplate jdbcTemplate;
    private final UpdatedStoryCacheService updatedStoryCacheService;
    private final StoryCacheService storyCacheService;
    private final QueueService queueService;
    private final CryptoUtil cryptoUtil;

    /*
     * 스토리 삭제 여부 체크
     * */
    private void validateStoryExistence(Long id) {
        boolean storyDeleted = deletedStoryCacheService.isDeleted(id);

        if (storyDeleted) {
            throw new StoryException(STORY_DELETED.getCode(), STORY_DELETED.getErrorMessage());
        }
    }

    /*
     * 스토리만 조회
     * */
    @Transactional(readOnly = true)
    public Story findByIdOnlyStory(Long id) {
        Optional<Story> findStory = storyRepository.findByIdOnlyStory(id);

        if (findStory.isEmpty()) {
            throw new StoryException(STORY_NOT_EXIST.getCode(), STORY_NOT_EXIST.getErrorMessage());
        }

        return findStory.get();
    }

    /*
     * 스토리 전체 조회
     * 재 로그인시 데이터 동기화 목적으로 호출
     * */
    @Transactional(readOnly = true)
    public StoryListItemResponseDto getStoryListAfterUpdate(Long coupleId) {
        List<Story> storyList = storyRepository.findByAllByCoupleId(coupleId);

        Set<Long> idListForDelete = storyList.stream().map(Story::getId).collect(Collectors.toSet());
        Set<Long> deleteIntersect = deletedStoryCacheService.findDeleteIntersect(idListForDelete);

        List<Story> filteredDeletedList = storyList.stream()
                .filter(story -> !deleteIntersect.contains(story.getId())).toList();

        List<String> idListForUpdate = filteredDeletedList.stream().map(story -> story.getId().toString()).toList();
        Map<Long, StoryItemResponseDto> updatedList = updatedStoryCacheService.findUpdatedList(idListForUpdate);

        List<StoryItemResponseDto> result = filteredDeletedList.stream()
                .map(story -> {
                    StoryItemResponseDto updatedDto = updatedList.get(story.getId());
                    if (updatedDto != null) {
                        return updatedDto;
                    }

                    return new StoryItemResponseDto(story);
                }).toList();

        return new StoryListItemResponseDto(result);
    }

    /*
     * 단일 스토리 생성
     * */
    public StoryItemResponseDto createStory(Member member, StoryCreateRequestDto dto) {
        Couple couple = member.getCouple();

        if (couple == null) {
            throw new StoryException(COUPLE_CAN_WRITE_STORY.getCode(), COUPLE_CAN_WRITE_STORY.getErrorMessage());
        }

        if (!Objects.equals(couple.getId(), dto.getCoupleId())) {
            throw new StoryException(COUPLE_CODE_INVALID.getCode(), COUPLE_CODE_INVALID.getErrorMessage());
        }

        Story story = new Story(dto, member.getId());
        Story savedStory = storyRepository.save(story);

        StoryItemResponseDto data = new StoryItemResponseDto(savedStory);

        MessageQueueRequestDto message = new MessageQueueRequestDto(data, member.getId(), INSERT);
        Long otherId = Objects.equals(couple.getFirstId(), member.getId()) ? couple.getSecondId() : couple.getFirstId();
        queueService.insert(otherId, message);
        return data;
    }

    /*
     * 벌크 스토리 생성
     * */
    public StoryListItemResponseDto createBulkStory(Member member, StoryCreateListRequestDto dto) {
        Couple couple = member.getCouple();

        if (couple == null) {
            throw new StoryException(COUPLE_CAN_WRITE_STORY.getCode(), COUPLE_CAN_WRITE_STORY.getErrorMessage());
        }

        if (!Objects.equals(couple.getId(), dto.getCoupleId())) {
            throw new StoryException(COUPLE_CODE_INVALID.getCode(), COUPLE_CODE_INVALID.getErrorMessage());
        }

        StoryListItemResponseDto data = bulkInsert(dto, member.getId());
        MessageQueueRequestDto message = new MessageQueueRequestDto(data, member.getId(), INSERT);
        Long otherId = Objects.equals(couple.getFirstId(), member.getId()) ? couple.getSecondId() : couple.getFirstId();
        queueService.insert(otherId, message);
        return data;
    }

    public StoryListItemResponseDto bulkInsert(StoryCreateListRequestDto dto, Long memberId) {
        String sql = "INSERT INTO story (story_id, couple_identifier, member_id, url, memo, " +
                "status, date, location, created_date, last_modified_date) " +
                "VALUES (:id, :coupleIdentifier, :memberId, :url, " +
                ":memo, :status, :date, :location, :createdDate, :lastModifiedDate)";

        LocalDateTime localDateTime = LocalDateTime.now();

        EncryptorConverter encryptorConverter = new EncryptorConverter(cryptoUtil);

        List<Story> storyList = new ArrayList<>();
        List<Story> returnList = new ArrayList<>();

        for (StoryCreateListItemRequestDto item : dto.getStoryList()) {
            long id = IDGenerator.generateId();
            Story returnedStory = new Story(id, item, dto.getCoupleId(), memberId, localDateTime);
            returnList.add(returnedStory);

            Story story = new Story(id, item, dto.getCoupleId(), memberId,
                    localDateTime, encryptorConverter.convertToDatabaseColumn(item.getUrl()));
            storyList.add(story);
        }

        SqlParameterSource[] batchParams = storyList.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(sql, batchParams);

        List<StoryItemResponseDto> bulkPlanItemResponseList = returnList.stream().map(StoryItemResponseDto::new).toList();
        return new StoryListItemResponseDto(bulkPlanItemResponseList);
    }

    /*
     * 스토리 수정
     *  */
    public StoryItemResponseDto updateStory(StoryUpdateRequestDto dto, Long memberId) {
        Lock lock = coupleLockService.storyLocks.computeIfAbsent(dto.getCoupleId(), id -> new ReentrantLock());
        lock.lock();
        try {
            validateStoryExistence(dto.getStoryId());
            Story story = findByIdOnlyStory(dto.getStoryId());

            if (!Objects.equals(story.getCoupleIdentifier(), dto.getCoupleId())) {
                throw new StoryException(UN_AUTH_STORY.getCode(), UN_AUTH_STORY.getErrorMessage());
            }

            if (Objects.equals(story.getStatus(), ITEM_DELETED)) {
                throw new StoryException(STORY_DELETED.getCode(), STORY_DELETED.getErrorMessage());
            }

            StoryItemResponseDto data = new StoryItemResponseDto(story, dto);
            MessageQueueRequestDto message = new MessageQueueRequestDto(data, memberId, UPDATE);

            storyCacheService.update(story, data);
            queueService.insert(dto.getOtherId(), message);
            return data;
        } finally {
            lock.unlock();
            coupleLockService.cleanupStoryLock(dto.getCoupleId(), lock);
        }
    }

    public void bulkUpdateStories() {
        Map<Long, StoryItemResponseDto> updatedStories = updatedStoryCacheService.getUpdatedStory();

        if (updatedStories.isEmpty()) {
            return;
        }

        String sql = "UPDATE story SET memo = ?, date = ?, location = ?, last_modified_date = ? WHERE story_id = ?";

        List<Object[]> batchArgs = updatedStories.values().stream()
                .map(dto -> new Object[]{
                        dto.getMemo(),
                        dto.getDate(),
                        dto.getLocation(),
                        dto.getLastModifiedDate(),
                        Long.parseLong(dto.getStoryId())
                }).toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);

        Set<Long> updatedStoryIds = updatedStories.keySet();
        updatedStoryCacheService.removeCache(updatedStoryIds);
    }

    /*
     * 스토리 삭제
     *  */
    public void deleteStory(List<Long> storyIds, Long otherId, Long memberId, Long coupleId) {
        Lock lock = coupleLockService.storyLocks.computeIfAbsent(coupleId, id -> new ReentrantLock());
        lock.lock();
        try {
            Set<Long> deletedStoryIds = deletedStoryCacheService.findDeleteIntersect(new HashSet<>(storyIds));

            if (!deletedStoryIds.isEmpty()) {
                throw new StoryException(STORY_ALREADY_DELETED.getCode(), STORY_ALREADY_DELETED.getErrorMessage());
            }

            List<String> itemList = storyIds.stream().map(String::valueOf).toList();
            StoryItemResponseDto data = new StoryItemResponseDto(itemList);
            MessageQueueRequestDto message = new MessageQueueRequestDto(itemList.get(0), data, memberId, DELETE);

            storyCacheService.delete(storyIds);
            queueService.insert(otherId, message);
        } finally {
            lock.unlock();
            coupleLockService.cleanupStoryLock(coupleId, lock);
        }
    }

    /*
     * 벌크 업데이트
     *  */
    @Scheduled(fixedRate = TEM_MINUTE)
    public void syncStoryToDatabase() {
        bulkUpdateStoryAsync()
                .thenCompose(result -> bulkUpdateDeleteStoryAsync())
                .thenRun(() -> {
                    System.out.println("스토리 비동기 처리가 완료되었습니다");
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Async
    public CompletableFuture<Void> bulkUpdateStoryAsync() {
        bulkUpdateStories();
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> bulkUpdateDeleteStoryAsync() {
        Set<Long> deletedIds = deletedStoryCacheService.getDeletedStory();
        if (!deletedIds.isEmpty()) {
            storyRepository.bulkUpdateStatusToFalse(new ArrayList<>(deletedIds));
            deletedStoryCacheService.removeCache(deletedIds);
        }
        return CompletableFuture.completedFuture(null);
    }
}

package com.example.naver.domain.service;

import com.example.naver.domain.dto.queue.req.MessageQueueRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateListItemRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateListRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateRequestDto;
import com.example.naver.domain.dto.story.req.StoryUpdateRequestDto;
import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.dto.story.res.StoryListItemResponseDto;
import com.example.naver.domain.entity.member.Couple;
import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.entity.story.Story;
import com.example.naver.domain.generator.IDGenerator;
import com.example.naver.domain.redis.queue.QueueService;
import com.example.naver.domain.redis.story.DeletedStoryCacheService;
import com.example.naver.domain.redis.story.StoryCacheService;
import com.example.naver.domain.redis.story.UpdatedStoryCacheService;
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
    private final IDGenerator idGenerator;
    private final DeletedStoryCacheService deletedStoryCacheService;
    private final StoryRepository storyRepository;
    private final CoupleLockService coupleLockService;
    private final JdbcTemplate jdbcTemplate;
    private final UpdatedStoryCacheService updatedStoryCacheService;
    private final StoryCacheService storyCacheService;
    private final QueueService queueService;
    private final CryptoUtil cryptoUtil;

    private static final String INSERT_STORY_SQL =
            "INSERT INTO story (story_id, couple_identifier, member_id, url, memo, " +
                    "status, date, location, created_date, last_modified_date) " +
                    "VALUES (:id, :coupleIdentifier, :memberId, :url, " +
                    ":memo, :status, :date, :location, :createdDate, :lastModifiedDate)";

    private static final String UPDATE_STORY_SQL =
            "UPDATE story SET memo = ?, date = ?, location = ?, last_modified_date = ? WHERE story_id = ?";

    /* 스토리 삭제 여부 체크 */
    private void validateStoryExistence(Long id) {
        boolean storyDeleted = deletedStoryCacheService.isDeleted(id);

        if (storyDeleted) {
            throw new StoryException(
                    STORY_DELETED.getCode(),
                    STORY_DELETED.getErrorMessage()
            );
        }
    }

    /* 스토리만 조회 */
    @Transactional(readOnly = true)
    public Story findByIdOnlyStory(Long id) {
        Optional<Story> findStory = storyRepository.findByIdOnlyStory(id);

        if (findStory.isEmpty()) {
            throw new StoryException(
                    STORY_NOT_EXIST.getCode(),
                    STORY_NOT_EXIST.getErrorMessage()
            );
        }

        return findStory.get();
    }

    /* 스토리 전체 조회 – 재로그인 시 데이터 동기화 */
    @Transactional(readOnly = true)
    public StoryListItemResponseDto getStoryListAfterUpdate(Long coupleId) {

       // 1️⃣ 커플의 모든 스토리 조회 - 아이디만 추출
        List<Story> storyList = storyRepository.findByAllByCoupleId(coupleId);

        Set<Long> idListForDelete = storyList.stream()
                .map(Story::getId)
                .collect(Collectors.toSet());

        // 2️⃣ 추출한 아이디로 삭제된 스토리 제거
        Set<Long> deleteIntersect = deletedStoryCacheService.findDeleteIntersect(idListForDelete);

        List<Story> filteredDeletedList = storyList.stream()
                .filter(story -> !deleteIntersect.contains(story.getId()))
                .toList();

        // 3️⃣ 남아있는 스토리 아이디 다시 추출 후 캐시 업데이트 상태 조회
        List<String> idListForUpdate = filteredDeletedList.stream()
                .map(story -> story.getId().toString())
                .toList();

        Map<Long, StoryItemResponseDto> updatedList =
                updatedStoryCacheService.findUpdatedList(idListForUpdate);

        // 4️⃣ 업데이트 된 것이 있다면 이를 반영
        List<StoryItemResponseDto> result = filteredDeletedList.stream()
                .map(story -> {
                    StoryItemResponseDto updatedDto =
                            updatedList.get(story.getId());

                    return (updatedDto != null)
                            ? updatedDto
                            : new StoryItemResponseDto(story);
                })
                .toList();

        return new StoryListItemResponseDto(result);
    }

    /* 단일 스토리 생성 후 메시지큐에 삽입 */
    public StoryItemResponseDto createStory(Member member, StoryCreateRequestDto dto) {
        Couple couple = member.getCouple();

        if (couple == null) {
            throw new StoryException(
                    COUPLE_CAN_WRITE_STORY.getCode(),
                    COUPLE_CAN_WRITE_STORY.getErrorMessage()
            );
        }

        if (!Objects.equals(couple.getId(), dto.getCoupleId())) {
            throw new StoryException(
                    COUPLE_CODE_INVALID.getCode(),
                    COUPLE_CODE_INVALID.getErrorMessage()
            );
        }

        Story story = new Story(dto, member.getId());
        Story savedStory = storyRepository.save(story);

        StoryItemResponseDto data = new StoryItemResponseDto(savedStory);

        MessageQueueRequestDto message = new MessageQueueRequestDto(
                data,
                member.getId(),
                INSERT
        );

        Long otherId = Objects.equals(couple.getFirstId(), member.getId())
                ? couple.getSecondId()
                : couple.getFirstId();

        queueService.insert(otherId, message);
        return data;
    }

    /* 벌크 스토리 생성 후 메시지큐에 삽입 */
    public StoryListItemResponseDto createBulkStory(Member member, StoryCreateListRequestDto dto) {
        Couple couple = member.getCouple();

        if (couple == null) {
            throw new StoryException(
                    COUPLE_CAN_WRITE_STORY.getCode(),
                    COUPLE_CAN_WRITE_STORY.getErrorMessage()
            );
        }

        if (!Objects.equals(couple.getId(), dto.getCoupleId())) {
            throw new StoryException(
                    COUPLE_CODE_INVALID.getCode(),
                    COUPLE_CODE_INVALID.getErrorMessage()
            );
        }

        StoryListItemResponseDto data = bulkInsert(dto, member.getId());

        MessageQueueRequestDto message = new MessageQueueRequestDto(
                data,
                member.getId(),
                INSERT
        );

        Long otherId = Objects.equals(couple.getFirstId(), member.getId())
                ? couple.getSecondId()
                : couple.getFirstId();

        queueService.insert(otherId, message);
        return data;
    }

    public StoryListItemResponseDto bulkInsert(StoryCreateListRequestDto dto, Long memberId) {

        LocalDateTime now = LocalDateTime.now();
        EncryptorConverter encryptorConverter = new EncryptorConverter(cryptoUtil);

        List<Story> storyList = new ArrayList<>();
        List<Story> returnList = new ArrayList<>();

        for (StoryCreateListItemRequestDto item : dto.getStoryList()) {
            long id = idGenerator.generateId();

            Story returnedStory = new Story(
                    id,
                    item,
                    dto.getCoupleId(),
                    memberId,
                    now
            );
            returnList.add(returnedStory);

            Story story = new Story(
                    id,
                    item,
                    dto.getCoupleId(),
                    memberId,
                    now,
                    encryptorConverter.convertToDatabaseColumn(item.getUrl())
            );
            storyList.add(story);
        }

        SqlParameterSource[] batchParams = storyList.stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(INSERT_STORY_SQL, batchParams);

        List<StoryItemResponseDto> responseList = returnList.stream()
                .map(StoryItemResponseDto::new)
                .toList();

        return new StoryListItemResponseDto(responseList);
    }

    /* 스토리 수정 후 메시지큐에 삽입 */
    public StoryItemResponseDto updateStory(StoryUpdateRequestDto dto, Long memberId) {
        Lock lock = coupleLockService.storyLocks
                .computeIfAbsent(dto.getCoupleId(), id -> new ReentrantLock());
        lock.lock();

        try {
            validateStoryExistence(dto.getStoryId());
            Story story = findByIdOnlyStory(dto.getStoryId());

            if (!Objects.equals(story.getCoupleIdentifier(), dto.getCoupleId())) {
                throw new StoryException(
                        UN_AUTH_STORY.getCode(),
                        UN_AUTH_STORY.getErrorMessage()
                );
            }

            if (Objects.equals(story.getStatus(), ITEM_DELETED)) {
                throw new StoryException(
                        STORY_DELETED.getCode(),
                        STORY_DELETED.getErrorMessage()
                );
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

    /* 스토리 삭제 후 메시지큐에 삽입 */
    public void deleteStory(List<Long> storyIds,
                            Long otherId,
                            Long memberId,
                            Long coupleId) {

        Lock lock = coupleLockService.storyLocks
                .computeIfAbsent(coupleId, id -> new ReentrantLock());
        lock.lock();

        try {
            Set<Long> deletedStoryIds =
                    deletedStoryCacheService.findDeleteIntersect(new HashSet<>(storyIds));

            if (!deletedStoryIds.isEmpty()) {
                throw new StoryException(
                        STORY_ALREADY_DELETED.getCode(),
                        STORY_ALREADY_DELETED.getErrorMessage()
                );
            }

            List<String> itemList = storyIds.stream()
                    .map(String::valueOf)
                    .toList();

            StoryItemResponseDto data = new StoryItemResponseDto(itemList);
            MessageQueueRequestDto message =
                    new MessageQueueRequestDto(itemList.get(0), data, memberId, DELETE);

            storyCacheService.delete(storyIds);
            queueService.insert(otherId, message);
        } finally {
            lock.unlock();
            coupleLockService.cleanupStoryLock(coupleId, lock);
        }
    }

    /* 벌크 업데이트 */
    @Scheduled(fixedRate = ONE_MINUTE)
    public void syncStoryToDatabase() {
        bulkUpdateStoryAsync();
        bulkUpdateDeleteStoryAsync();
    }

    @Async
    public void bulkUpdateStoryAsync() {
        bulkUpdateStories();
    }

    @Async
    public void bulkUpdateDeleteStoryAsync() {
        Set<Long> deletedIds = deletedStoryCacheService.getDeletedStory();
        if (!deletedIds.isEmpty()) {
            storyRepository.bulkUpdateStatusToFalse(new ArrayList<>(deletedIds));
            deletedStoryCacheService.removeCache(deletedIds);
        }
    }

    public void bulkUpdateStories() {
        Map<Long, StoryItemResponseDto> updatedStories =
                updatedStoryCacheService.getUpdatedStory();

        if (updatedStories.isEmpty()) {
            return;
        }

        List<Object[]> batchArgs = updatedStories.values().stream()
                .map(dto -> new Object[]{
                        dto.getMemo(),
                        dto.getDate(),
                        dto.getLocation(),
                        dto.getLastModifiedDate(),
                        Long.parseLong(dto.getStoryId())
                }).toList();

        jdbcTemplate.batchUpdate(UPDATE_STORY_SQL, batchArgs);

        Set<Long> updatedStoryIds = updatedStories.keySet();
        updatedStoryCacheService.removeCache(updatedStoryIds);
    }
}

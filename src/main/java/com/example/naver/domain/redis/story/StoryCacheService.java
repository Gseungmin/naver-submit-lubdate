package com.example.naver.domain.redis.story;

import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.entity.story.Story;
import com.example.naver.domain.repository.StoryRepository;
import com.example.naver.domain.service.SlackService;
import com.example.naver.web.exception.infra.InfraException;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.naver.web.exception.ExceptionType.REDIS_CONNECT_ERROR;
import static com.example.naver.web.exception.ExceptionType.REDIS_WRITE_ERROR;
import static com.example.naver.web.util.Util.DELETED_STORY_CACHE;
import static com.example.naver.web.util.Util.UPDATE_STORY_CACHE;

@Service
@RequiredArgsConstructor
@Transactional
public class StoryCacheService {

    private final RedisTemplate<String, Object> objectTemplate;
    private final RedisTemplate<String, String> stringTemplate;
    private final SlackService slackService;
    private final StoryRepository storyRepository;

    /*
     * 벌크 업데이트를 위해 스토리 업데이트 데이터 생성
     * 1. 데이터 레디스에 저장 - 추후 벌크 업데이트로 활용
     * 2. 만약 저장이 안될 시 이를 바로 데이터베이스에 반영
     * */
    public void update(Story story, StoryItemResponseDto data) {
        try {
            objectTemplate.opsForHash()
                    .put(UPDATE_STORY_CACHE,
                            String.valueOf(story.getId()),
                            data);

        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            story.updateStory(data);
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);

        } catch (Exception e) {
            throw new InfraException(
                    REDIS_WRITE_ERROR.getCode(),
                    REDIS_WRITE_ERROR.getErrorMessage()
            );
        }
    }

    /*
     * 벌크 업데이트를 위해 스토리 삭제 데이터 생성
     * 1. 데이터 레디스에 저장 - 추후 벌크 업데이트로 활용
     * 2. 만약 저장이 안될 시 이를 바로 데이터베이스에 반영
     * */
    public void delete(List<Long> storyIds) {
        try {
            String[] members = storyIds.stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);

            stringTemplate.opsForSet()
                    .add(DELETED_STORY_CACHE, members);

        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            storyRepository.bulkUpdateStatusToFalse(storyIds);
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);

        } catch (Exception e) {
            throw new InfraException(
                    REDIS_WRITE_ERROR.getCode(),
                    REDIS_WRITE_ERROR.getErrorMessage()
            );
        }
    }
}
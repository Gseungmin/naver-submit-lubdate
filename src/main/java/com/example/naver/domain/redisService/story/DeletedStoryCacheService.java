package com.example.naver.domain.redisService.story;

import com.example.naver.domain.service.SlackService;
import com.example.naver.web.exception.infra.InfraException;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.DELETED_STORY_CACHE;

@Service
@RequiredArgsConstructor
public class DeletedStoryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SlackService slackService;

    /*
     * 벌크 업데이트를 위해 스토리 아이디 셋 조회
     * 1. 내용 조회 후 벌크 업데이트 진행
     * */
    public Set<Long> getDeletedStory() {
        try {
            Set<Object> storyIds = redisTemplate.opsForSet().members(DELETED_STORY_CACHE);

            if (storyIds == null) {
                return new HashSet<>();
            }

            return storyIds.stream()
                    .filter(Objects::nonNull)
                    .map(member -> Long.valueOf(member.toString()))
                    .collect(Collectors.toSet());
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
            return new HashSet<>();
        } catch (Exception e) {
            throw new InfraException(REDIS_DELETE_CHECK_ERROR.getCode(), REDIS_DELETE_CHECK_ERROR.getErrorMessage());
        }
    }

    /*
     * 삭제된 스토리 아이디 조회
     * 1. 스토리 삭제 및 업데이트 전 스토리가 이미 삭제되었는지 체크
     * */
    public boolean isDeleted(Long storyId) {
        try {
            Boolean inRedis = redisTemplate.opsForSet().isMember(DELETED_STORY_CACHE, storyId);
            return Boolean.TRUE.equals(inRedis);
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
            return false;
        } catch (Exception e) {
            throw new InfraException(REDIS_DELETE_CHECK_ERROR.getCode(), REDIS_DELETE_CHECK_ERROR.getErrorMessage());
        }
    }

    /*
     * 삭제 후 캐시 삭제
     * 1. 벌크 업데이트 후 스토리 캐시를 삭제
     * */
    public void removeCache(Set<Long> ids) {
        try {
            redisTemplate.opsForSet().remove(DELETED_STORY_CACHE, ids.toArray());
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
        } catch (Exception e) {
            throw new InfraException(REDIS_DELETE_CACHE_REMOVE_ERROR.getCode(), REDIS_DELETE_CACHE_REMOVE_ERROR.getErrorMessage());
        }
    }

    /*
     * 삭제 체크 메서드
     * 1. 스토리가 삭제되었는지 체크하는 메서드
     * 2. DB에서 데이터 조회했을때 해당 아이디들에 대해서 삭제 반영
     * */
    public Set<Long> findDeleteIntersect(Set<Long> ids) {
        RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();
        try {
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Long id : ids) {
                    connection.setCommands().sIsMember(
                            keySerializer.serialize(DELETED_STORY_CACHE),
                            keySerializer.serialize(id.toString())
                    );
                }
                return null;
            });

            Set<Long> deletedStoryIds = new HashSet<>();
            int index = 0;
            for (Long id : ids) {
                Boolean isDeleted = (Boolean) results.get(index++);
                if (Boolean.TRUE.equals(isDeleted)) {
                    deletedStoryIds.add(id);
                }
            }

            return deletedStoryIds;
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
            return new HashSet<>();
        } catch (Exception e) {
            throw new InfraException(REDIS_DELETE_CHECK_ERROR.getCode(), REDIS_DELETE_CHECK_ERROR.getErrorMessage());
        }
    }
}

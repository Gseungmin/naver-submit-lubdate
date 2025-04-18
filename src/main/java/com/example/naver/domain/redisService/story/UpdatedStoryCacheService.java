package com.example.naver.domain.redisService.story;

import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.web.exception.infra.InfraException;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.UPDATE_STORY_CACHE;

@Service
@RequiredArgsConstructor
public class UpdatedStoryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public Map<Long, StoryItemResponseDto> getUpdatedStory() {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(UPDATE_STORY_CACHE);
            Map<Long, StoryItemResponseDto> result = new HashMap<>();
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                Long id = Long.valueOf((String) entry.getKey());
                StoryItemResponseDto data = (StoryItemResponseDto) entry.getValue();
                result.put(id, data);
            }
            return result;
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_UPDATE_CHECK_ERROR.getCode(), REDIS_UPDATE_CHECK_ERROR.getErrorMessage());
        }
    }

    public void removeCache(Set<Long> ids) {
        try {
            redisTemplate.opsForHash().delete(UPDATE_STORY_CACHE, ids.stream().map(Object::toString).toArray());
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_UPDATE_CACHE_REMOVE_ERROR.getCode(), REDIS_UPDATE_CACHE_REMOVE_ERROR.getErrorMessage());
        }
    }

    public Map<Long, StoryItemResponseDto> findUpdatedList(List<String> ids) {
        try {
            HashOperations<String, String, StoryItemResponseDto> hashOps = redisTemplate.opsForHash();
            List<StoryItemResponseDto> cachedDataList = hashOps.multiGet(UPDATE_STORY_CACHE, ids);

            Map<Long, StoryItemResponseDto> updatedMap = new HashMap<>();
            for (int i = 0; i < ids.size(); i++) {
                StoryItemResponseDto cachedData = cachedDataList.get(i);
                if (cachedData != null) {
                    Long id = Long.valueOf(ids.get(i));
                    updatedMap.put(id, new StoryItemResponseDto(cachedData));
                }
            }
            return updatedMap;
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_UPDATE_CHECK_ERROR.getCode(), REDIS_UPDATE_CHECK_ERROR.getErrorMessage());
        }
    }
}
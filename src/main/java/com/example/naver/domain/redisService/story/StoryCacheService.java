package com.example.naver.domain.redisService.story;

import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.web.exception.infra.InfraException;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.DELETED_STORY_CACHE;
import static com.example.naver.web.util.Util.UPDATE_STORY_CACHE;

@Service
@RequiredArgsConstructor
public class StoryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void update(Long storyId, StoryItemResponseDto data) {
        try {
            RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();
            RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();

            redisTemplate.executePipelined((RedisCallback<?>) connection -> {
                connection.multi();
                connection.hashCommands().hSet(
                        keySerializer.serialize(UPDATE_STORY_CACHE),
                        keySerializer.serialize(storyId.toString()),
                        valueSerializer.serialize(data)
                );
                connection.close();
                return null;
            });
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_WRITE_ERROR.getCode(), REDIS_WRITE_ERROR.getErrorMessage());
        }
    }

    public void delete(List<Long> storyIds) {
        try {
            RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (Long storyId : storyIds) {
                    connection.setCommands().sAdd(
                            keySerializer.serialize(DELETED_STORY_CACHE),
                            keySerializer.serialize(storyId.toString())
                    );
                }
                return null;
            });
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_WRITE_ERROR.getCode(), REDIS_WRITE_ERROR.getErrorMessage());
        }
    }
}
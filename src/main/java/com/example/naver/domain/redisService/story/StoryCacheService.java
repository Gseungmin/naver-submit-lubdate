package com.example.naver.domain.redisService.story;

import com.example.naver.domain.dto.MessageQueueRequestDto;
import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.redisService.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.naver.web.util.Util.DELETED_STORY_CACHE;
import static com.example.naver.web.util.Util.UPDATE_STORY_CACHE;

@Service
@RequiredArgsConstructor
public class StoryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final QueueService queueService;

    public void update(Long storyId, Long otherId, StoryItemResponseDto data, MessageQueueRequestDto message) {
        RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();
        RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();

        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            connection.hashCommands().hSet(
                    keySerializer.serialize(UPDATE_STORY_CACHE),
                    keySerializer.serialize(storyId.toString()),
                    valueSerializer.serialize(data)
            );
            return null;
        });

        queueService.insert(otherId, message);
    }

    public void delete(List<Long> storyIds, Long otherId, MessageQueueRequestDto message) {
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

        queueService.insert(otherId, message);
    }
}
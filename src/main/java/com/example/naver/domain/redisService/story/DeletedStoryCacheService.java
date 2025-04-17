package com.example.naver.domain.redisService.story;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.naver.web.util.Util.DELETED_STORY_CACHE;

@Service
@RequiredArgsConstructor
public class DeletedStoryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public Set<Long> getDeletedStory() {
        Set<Object> storyIds = redisTemplate.opsForSet().members(DELETED_STORY_CACHE);
        if (storyIds == null) {
            return new HashSet<>();
        }
        return storyIds.stream().filter(Objects::nonNull)
                .map(member -> Long.valueOf(member.toString())).collect(Collectors.toSet());
    }

    public boolean isDeleted(Long storyId) {
        Boolean isDeleted = redisTemplate.opsForSet().isMember(DELETED_STORY_CACHE, storyId);
        return Boolean.TRUE.equals(isDeleted);
    }

    public void clearCache() {
        redisTemplate.delete(DELETED_STORY_CACHE);
    }

    public void removeCache(Set<Long> ids) {
        redisTemplate.opsForSet().remove(DELETED_STORY_CACHE, ids.toArray());
    }

    public Set<Long> findDeleteIntersect(Set<Long> ids) {
        RedisSerializer<String> keySerializer = redisTemplate.getStringSerializer();

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
    }
}

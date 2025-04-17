package com.example.naver.domain.redisService.story;

import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.naver.web.util.Util.UPDATE_STORY_CACHE;

@Service
@RequiredArgsConstructor
public class UpdatedStoryCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public Map<Long, StoryItemResponseDto> getUpdatedStory() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(UPDATE_STORY_CACHE);
        Map<Long, StoryItemResponseDto> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long id = Long.valueOf((String) entry.getKey());
            StoryItemResponseDto data = (StoryItemResponseDto) entry.getValue();
            result.put(id, data);
        }
        return result;
    }

    public void clearCache() {
        redisTemplate.delete(UPDATE_STORY_CACHE);
    }

    public void removeCache(Set<Long> ids) {
        redisTemplate.opsForHash().delete(UPDATE_STORY_CACHE, ids.stream().map(Object::toString).toArray());
    }

    public Map<Long, StoryItemResponseDto> findUpdatedList(List<String> ids) {
        HashOperations<String, String, StoryItemResponseDto> hashOperations = redisTemplate.opsForHash();
        List<StoryItemResponseDto> cachedDataList = hashOperations.multiGet(UPDATE_STORY_CACHE, ids);

        Map<Long, StoryItemResponseDto> updatedMap = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            StoryItemResponseDto cachedData = cachedDataList.get(i);
            if (cachedData != null) {
                Long id = Long.valueOf(ids.get(i));
                updatedMap.put(id, new StoryItemResponseDto(cachedData));
            }
        }

        return updatedMap;
    }
}
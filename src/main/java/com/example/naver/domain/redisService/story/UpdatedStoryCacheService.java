package com.example.naver.domain.redisService.story;

import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.service.SlackService;
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
    private final SlackService slackService;

    /*
     * 벌크 업데이트를 위해 스토리 해시 조회
     * 1. 내용 조회 후 벌크 업데이트 진행
     * */
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
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
            return new HashMap<>();
        } catch (Exception e) {
            throw new InfraException(REDIS_UPDATE_CHECK_ERROR.getCode(), REDIS_UPDATE_CHECK_ERROR.getErrorMessage());
        }
    }

    /*
     * 삭제 후 캐시 삭제
     * 1. 벌크 업데이트 후 스토리 캐시를 삭제
     * */
    public void removeCache(Set<Long> ids) {
        try {
            redisTemplate.opsForHash().delete(UPDATE_STORY_CACHE, ids.stream().map(Object::toString).toArray());
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
        } catch (Exception e) {
            throw new InfraException(REDIS_UPDATE_CACHE_REMOVE_ERROR.getCode(), REDIS_UPDATE_CACHE_REMOVE_ERROR.getErrorMessage());
        }
    }

    /*
     * 업데이트 체크 메서드
     * 1. 스토리가 업데이트 되었는지 체크하는 메서드
     * 2. DB에서 데이터 조회했을때 해당 아이디들에 대해서 업데이트 반영
     * */
    public Map<Long, StoryItemResponseDto> findUpdatedList(List<String> ids) {
        try {
            HashOperations<String, String, StoryItemResponseDto> hashOps = redisTemplate.opsForHash();
            List<StoryItemResponseDto> cachedDataList = hashOps.multiGet(UPDATE_STORY_CACHE, ids);

            Map<Long, StoryItemResponseDto> result = new HashMap<>();
            for (int i = 0; i < ids.size(); i++) {
                StoryItemResponseDto cachedData = cachedDataList.get(i);
                if (cachedData != null) {
                    Long id = Long.valueOf(ids.get(i));
                    result.put(id, new StoryItemResponseDto(cachedData));
                }
            }

            return result;
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
            return new HashMap<>();
        } catch (Exception e) {
            throw new InfraException(REDIS_UPDATE_CHECK_ERROR.getCode(), REDIS_UPDATE_CHECK_ERROR.getErrorMessage());
        }
    }
}
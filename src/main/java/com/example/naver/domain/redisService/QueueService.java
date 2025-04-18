package com.example.naver.domain.redisService;

import com.example.naver.domain.dto.MessageQueueRequestDto;
import com.example.naver.web.exception.infra.InfraException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisCommandTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.QUEUE_PREFIX;
import static com.example.naver.web.util.Util.QUEUE_SEQ_PREFIX;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, String> redisTemplateForQueue;
    private final ObjectMapper objectMapper;

    public Long insert(Long memberId, MessageQueueRequestDto message) {
        String queueCacheKey = QUEUE_PREFIX + memberId.toString();
        String queueSeqKey = QUEUE_SEQ_PREFIX + memberId;

        try {
            String messageJson = objectMapper.writeValueAsString(message);

            String luaScript = ""
                    + "local seq = redis.call('INCR', KEYS[1]); "
                    + "redis.call('ZADD', KEYS[2], seq, ARGV[1]); "
                    + "redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2])); "
                    + "redis.call('EXPIRE', KEYS[2], tonumber(ARGV[2])); "
                    + "return seq;";

            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(luaScript);
            script.setResultType(Long.class);


            Long sequenceNumber = redisTemplateForQueue.execute(
                    script,
                    Arrays.asList(queueSeqKey, queueCacheKey),
                    messageJson,
                    String.valueOf(Duration.ofDays(60).getSeconds())
            );

            return sequenceNumber;
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_INSERT_ERROR.getCode(), REDIS_INSERT_ERROR.getErrorMessage());
        }
    }

    public List<MessageQueueRequestDto> getMemberQueue(Long memberId) {
        String queueCacheKey = QUEUE_PREFIX + memberId.toString();
        List<MessageQueueRequestDto> result = new ArrayList<>();

        try {
            Set<ZSetOperations.TypedTuple<String>> entries = redisTemplateForQueue.opsForZSet().rangeWithScores(queueCacheKey, 0, -1);

            if (entries == null || entries.isEmpty()) {
                return result;
            }

            for (ZSetOperations.TypedTuple<String> entry : entries) {
                String messageJson = entry.getValue();
                Double score = entry.getScore();

                if (messageJson != null && !messageJson.isEmpty()) {
                    MessageQueueRequestDto dto = objectMapper.readValue(messageJson, MessageQueueRequestDto.class);
                    dto.setSequenceNumber(score.longValue());
                    result.add(dto);
                }
            }
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_GET_ERROR.getCode(), REDIS_GET_ERROR.getErrorMessage());
        }

        return result;
    }

    public void removeMemberQueue(Long memberId, Long lastSequenceNumber) {
        String queueCacheKey = QUEUE_PREFIX + memberId.toString();
        try {
            redisTemplateForQueue.opsForZSet().removeRangeByScore(queueCacheKey, 0, lastSequenceNumber);
        } catch (RedisConnectionFailureException e) {
            throw new InfraException(REDIS_CONNECT_ERROR.getCode(), REDIS_CONNECT_ERROR.getErrorMessage());
        } catch (RedisCommandTimeoutException e) {
            throw new InfraException(REDIS_TIMEOUT_ERROR.getCode(), REDIS_TIMEOUT_ERROR.getErrorMessage());
        } catch (Exception e) {
            throw new InfraException(REDIS_DELETE_ERROR.getCode(), REDIS_DELETE_ERROR.getErrorMessage());
        }
    }
}

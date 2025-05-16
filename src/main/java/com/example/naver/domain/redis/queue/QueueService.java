package com.example.naver.domain.redis.queue;

import com.example.naver.domain.dto.queue.req.MessageQueueRequestDto;
import com.example.naver.domain.service.SlackService;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.QUEUE_PREFIX;
import static com.example.naver.web.util.Util.QUEUE_SEQ_PREFIX;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, String> redisTemplateForQueue;
    private final SlackService slackService;
    private final ObjectMapper objectMapper;

    private final Map<Long, List<MessageQueueRequestDto>> localCacheQueue =
            new ConcurrentHashMap<>();

    private static final long FALLBACK_SEQ = -1L;
    private static final String INSERT_LUA = """
        local seq = redis.call('INCR', KEYS[1])
        redis.call('ZADD', KEYS[2], seq, ARGV[1])
        redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
        redis.call('EXPIRE', KEYS[2], tonumber(ARGV[2]))
        return seq
        """;


    /*
    * 메시지 큐 삽입 메서드
    * 1️⃣ LUA 스크립트로 시퀀스 증가 & ZSET 삽입
    * 2️⃣ Redis 장애 시 로컬 캐시에 임시 저장
    * */
    public void insert(Long memberId, MessageQueueRequestDto message) {
        String queueKey = QUEUE_PREFIX + memberId;
        String seqKey = QUEUE_SEQ_PREFIX + memberId;

        try {
            String msgJson = objectMapper.writeValueAsString(message);
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(INSERT_LUA, Long.class);

            redisTemplateForQueue.execute(
                    script,
                    Arrays.asList(seqKey, queueKey),
                    msgJson,
                    String.valueOf(Duration.ofDays(60).getSeconds())
            );

        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            message.setSequenceNumber(FALLBACK_SEQ);
            cacheLocally(memberId, message);
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);

        } catch (Exception e) {
            throw new InfraException(
                    REDIS_INSERT_ERROR.getCode(),
                    REDIS_INSERT_ERROR.getErrorMessage()
            );
        }
    }

    /*
     * 메시지 큐 조회 메서드
     * 1️⃣ ZSET에서 조회하며 score → sequenceNumber 로 주입
     * 2️⃣ 로컬 캐시에 데이터가 있으면 함께 반환
     * */
    public List<MessageQueueRequestDto> getMemberQueue(Long memberId) {
        String queueCacheKey = QUEUE_PREFIX + memberId.toString();
        List<MessageQueueRequestDto> result = new ArrayList<>();

        try {
            Set<ZSetOperations.TypedTuple<String>> entries =
                    redisTemplateForQueue.opsForZSet().rangeWithScores(queueCacheKey, 0, -1);

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

        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);

        } catch (Exception e) {
            throw new InfraException(
                    REDIS_GET_ERROR.getCode(),
                    REDIS_GET_ERROR.getErrorMessage()
            );
        }

        result.addAll(getLocalCache(memberId));
        return result;
    }

    /*
     * 메시지 큐 삭제 메서드
     * 1️⃣ 큐에서 메시지를 삭제
     * 2️⃣ 만약 가장큰 시퀀스 넘버가 -1L이면, 로컬 캐시에서만 조회하므로 로컬 캐시만 삭제
     * */
    public void removeMemberQueue(Long memberId, Long lastSequenceNumber) {
        if (lastSequenceNumber.equals(FALLBACK_SEQ)) {
            clearLocalCache(memberId);
            return;
        }

        String queueCacheKey = QUEUE_PREFIX + memberId.toString();
        try {
            redisTemplateForQueue.opsForZSet()
                    .removeRangeByScore(queueCacheKey, 0, lastSequenceNumber);

        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);

        } catch (Exception e) {
            throw new InfraException(
                    REDIS_DELETE_ERROR.getCode(),
                    REDIS_DELETE_ERROR.getErrorMessage()
            );

        } finally {
            clearLocalCache(memberId);
        }
    }

    private void cacheLocally(Long memberId, MessageQueueRequestDto message) {
        localCacheQueue
                .computeIfAbsent(memberId, id -> Collections.synchronizedList(new ArrayList<>()))
                .add(message);
    }

    private List<MessageQueueRequestDto> getLocalCache(Long memberId) {
        return localCacheQueue.getOrDefault(memberId, Collections.emptyList());
    }

    private void clearLocalCache(Long memberId) {
        localCacheQueue.remove(memberId);
    }
}

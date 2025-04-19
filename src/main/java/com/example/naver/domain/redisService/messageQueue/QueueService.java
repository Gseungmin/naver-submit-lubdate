package com.example.naver.domain.redisService.messageQueue;

import com.example.naver.domain.dto.MessageQueueRequestDto;
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
    private final Map<Long, List<MessageQueueRequestDto>> localCacheQueue = new ConcurrentHashMap<>();
    private final long FALLBACK_SEQ = -1L;

    /*
    * 메시지 큐 삽입 메서드
    * 1. 시퀀스 넘버와 큐의 동시 관리를 위해 LUA SCRIPT 활용해서 삽입
    * 2. 레디스 연결 싪패를 대비하여, 로컬 캐시를 사용 - 만약 레디스 장애로 인해 삽입 실패시 로컬에 임시 저장을 통해 가용성 확보
    * */
    public void insert(Long memberId, MessageQueueRequestDto message) {
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

            redisTemplateForQueue.execute(
                    script,
                    Arrays.asList(queueSeqKey, queueCacheKey),
                    messageJson,
                    String.valueOf(Duration.ofDays(60).getSeconds())
            );
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            message.setSequenceNumber(FALLBACK_SEQ);
            cacheLocally(memberId, message);
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
        } catch (Exception e) {
            throw new InfraException(REDIS_INSERT_ERROR.getCode(), REDIS_INSERT_ERROR.getErrorMessage());
        }
    }

    /*
     * 메시지 큐 조회 메서드
     * 1. 큐에서 메시지 조회하면서 메시지마다 시퀀스 넘버 주입
     * 2. 만약 로컬 캐시된 데이터가 있다면 같이 조회
     * */
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
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
        } catch (Exception e) {
            throw new InfraException(REDIS_GET_ERROR.getCode(), REDIS_GET_ERROR.getErrorMessage());
        }

        List<MessageQueueRequestDto> localList = getLocalCache(memberId);
        result.addAll(localList);
        return result;
    }

    /*
     * 메시지 큐 삭제 메서드
     * 1. 큐에서 메시지를 삭제
     * 2. 만약 가장큰 시퀀스 넘버가 -1L이면, 로컬 캐시에서만 조회하므로 로컬 캐시만 삭제
     * */
    public void removeMemberQueue(Long memberId, Long lastSequenceNumber) {
        if (lastSequenceNumber.equals(-1L)) {
            clearLocalCache(memberId);
            return;
        }

        String queueCacheKey = QUEUE_PREFIX + memberId.toString();
        try {
            redisTemplateForQueue.opsForZSet().removeRangeByScore(queueCacheKey, 0, lastSequenceNumber);
        } catch (RedisConnectionFailureException | RedisCommandTimeoutException e) {
            slackService.sendRedisErrorMessage(REDIS_CONNECT_ERROR);
        } catch (Exception e) {
            throw new InfraException(REDIS_DELETE_ERROR.getCode(), REDIS_DELETE_ERROR.getErrorMessage());
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

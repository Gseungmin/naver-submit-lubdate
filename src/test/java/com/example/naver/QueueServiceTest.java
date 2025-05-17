package com.example.naver;

import com.example.naver.domain.dto.queue.req.MessageQueueRequestDto;
import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.redis.queue.QueueService;
import com.example.naver.domain.service.SlackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.naver.web.util.Util.INSERT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SlackService slackService;

    private QueueService queueService;

    private Long senderId;
    private Long receiverId;
    private MessageQueueRequestDto message;

    @BeforeEach
    void setUp() {
        queueService = new QueueService(redisTemplate, slackService, new ObjectMapper());

        senderId = 1L;
        receiverId = 2L;

        StoryItemResponseDto storyInfo = new StoryItemResponseDto(List.of("1","2","3"));
        message = new MessageQueueRequestDto(storyInfo, senderId, INSERT);

        /* INSERT 시 레디스 장애 */
        when(redisTemplate.execute(
                any(DefaultRedisScript.class),
                anyList(),
                any(), any()))
                .thenThrow(new RedisConnectionFailureException("레디스 장애 발생"));

        queueService.insert(receiverId, message);
    }

    @Test
    @DisplayName("1. 레디스 장애 시 로컬 캐시에 저장된다")
    void 레디스_장애시_로컬캐시() {
        // when // then
        assertThat(message.getSequenceNumber()).isEqualTo(-1L);
        assertThat(queueService.getLocalCache(receiverId)).hasSize(1);
    }

    @Test
    @DisplayName("2. 레디스 장애 시 로컬 캐시만 조회한다")
    void 레디스_장애시_로컬캐시만_조회() {
        // given
        when(redisTemplate.opsForZSet()
                .rangeWithScores(anyString(), anyLong(), anyLong()))
                .thenThrow(new RedisConnectionFailureException("레디스 장애"));

        // when
        List<MessageQueueRequestDto> result = queueService.getMemberQueue(receiverId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSequenceNumber()).isEqualTo(-1L);
    }

    @Test
    @DisplayName("3. 레디스 장애로 인한 데이터 조회 시 로컬 캐시만 삭제 조회한다")
    void 레디스_장애_데이터만_조회시_로컬캐시만_삭제() {
        // given
        when(redisTemplate.opsForZSet()
                .rangeWithScores(anyString(), anyLong(), anyLong()))
                .thenThrow(new RedisConnectionFailureException("레디스 장애"));

        List<MessageQueueRequestDto> dataList = queueService.getMemberQueue(receiverId);
        Long delete_sequence_id = dataList.get(0).getSequenceNumber();

        // when then
        assertDoesNotThrow(() ->
                queueService.removeMemberQueue(receiverId, delete_sequence_id));
    }
}
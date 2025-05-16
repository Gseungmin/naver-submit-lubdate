package com.example.naver.web.controller;

import com.example.naver.domain.dto.queue.req.AckRequestDto;
import com.example.naver.domain.dto.queue.req.MessageQueueRequestDto;
import com.example.naver.domain.redis.queue.QueueService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.naver.web.util.FunctionUtil.parseLong;
import static com.example.naver.web.util.MemberValidator.validateAuthentication;

@RestController
@RequiredArgsConstructor
@RequestMapping("/couple")
public class CoupleController {

    private final QueueService queueService;

    /*데이터 동기화*/
    @GetMapping("/synchronize")
    public List<MessageQueueRequestDto> getSynchronize(
            Authentication authentication,
            HttpServletRequest request
    ) {
        validateAuthentication(authentication, request);
        Long memberId = parseLong(authentication.getName());
        return queueService.getMemberQueue(memberId);
    }

    /*데이터 동기화*/
    @PostMapping("/synchronize-ack")
    public void ackSynchronize(
            @RequestBody AckRequestDto dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        validateAuthentication(authentication, request);
        Long memberId = parseLong(authentication.getName());
        queueService.removeMemberQueue(memberId, dto.getSequenceNumber());
    }
}

package com.example.naver.domain.service;

import com.example.naver.web.exception.ExceptionType;
import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.model.Field;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.example.naver.web.util.Util.SLACK_WEBHOOK_URL;
import static com.slack.api.webhook.WebhookPayloads.payload;


@Service
public class SlackService {

    private final Slack slackClient = Slack.getInstance();

    @Async
    public void sendBadRequestMessage(HttpServletRequest request, ExceptionType exception) {
        String title = "🚨 악의적인 요청이 들어왔습니다.";
        Map<String, String> data = new HashMap<>();
        data.put("에러 메시지", exception.getErrorMessage());
        data.put("요청 URI", request.getRequestURI());
        data.put("요청 IP", request.getRemoteAddr());
        data.put("Timestamp", new Date().toString());
        data.put("Request Details", logRequestDetails(request));
        sendMessage(title, data);
    }

    @Async
    public void sendRedisErrorMessage(ExceptionType exception) {
        String title = "🛑 Redis Cache Error 발생";
        Map<String, String> data = new HashMap<>();
        data.put("에러 코드", String.valueOf(exception.getCode()));
        data.put("에러 메시지", exception.getErrorMessage());
        data.put("발생 시각", new Date().toString());
        sendMessage(title, data);
    }

    private void sendMessage(String title, Map<String, String> data) {
        try {
            slackClient.send(SLACK_WEBHOOK_URL, payload(p -> p
                    .text(title)
                    .attachments(List.of(
                            Attachment.builder()
                                    .fields(
                                            data.keySet().stream().map(key -> generateSlackField(key, data.get(key))).toList()
                                    ).build())))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String logRequestDetails(HttpServletRequest request) {
        StringBuilder details = new StringBuilder();

        details.append("요청 메소드: ").append(request.getMethod()).append("\n");
        details.append("요청 URL: ").append(request.getRequestURL()).append("\n");

        details.append("헤더 정보:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            details.append("  ").append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
        }

        details.append("파라미터 정보:\n");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            details.append("  ").append(paramName).append(": ").append(request.getParameter(paramName)).append("\n");
        }

        if (request.getSession(false) != null) {
            details.append("세션 정보:\n");
            Enumeration<String> sessionAttrNames = request.getSession().getAttributeNames();
            while (sessionAttrNames.hasMoreElements()) {
                String attrName = sessionAttrNames.nextElement();
                details.append("  ").append(attrName).append(": ").append(request.getSession().getAttribute(attrName)).append("\n");
            }
        }

        details.append("요청 프로토콜: ").append(request.getProtocol()).append("\n");
        details.append("컨텐츠 타입: ").append(request.getContentType()).append("\n");
        details.append("인증 타입: ").append(request.getAuthType()).append("\n");
        details.append("리모트 호스트: ").append(request.getRemoteHost()).append("\n");
        details.append("리모트 유저: ").append(request.getRemoteUser()).append("\n");
        return details.toString();
    }

    private Field generateSlackField(String title, String value) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(false)
                .build();
    }
}
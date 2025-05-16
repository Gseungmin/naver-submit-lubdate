package com.example.naver.web.filter.exception;

import com.example.naver.domain.service.SlackService;
import com.example.naver.web.exception.ExceptionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.example.naver.web.exception.ExceptionType.BLOCKED_IP;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SlackService slackService;

    @Value("${server.type}")
    private String serverType;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ExceptionType exception = (ExceptionType) request.getAttribute("exception");

        if (exception != null
                && Objects.equals(exception.getCode(), BLOCKED_IP.getCode())
                && serverType.equalsIgnoreCase("prod")) {

            slackService.sendBadRequestMessage(request, exception);
        }

        setResponse(response, exception);
    }

    private void setResponse(HttpServletResponse response, ExceptionType errorType) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        if (errorType == null) {
            errorType = ExceptionType.UNKNOWN_ERROR;
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorMessage", errorType.getErrorMessage());
        errorResponse.put("code", errorType.getCode());

        String jsonResponse = mapper.writeValueAsString(errorResponse);
        response.getWriter().print(jsonResponse);
    }
}

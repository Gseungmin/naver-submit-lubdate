package com.example.naver.web.filter.exception;

import com.example.naver.web.exception.member.LoginException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

import static com.example.naver.web.exception.ExceptionType.LOGIN_FAILED;

@Component
@Slf4j
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        if (exception instanceof UsernameNotFoundException
                || exception instanceof BadCredentialsException) {

            sendErrorResponse(response,
                    LOGIN_FAILED.getErrorMessage(),
                    LOGIN_FAILED.getCode());
            return;
        }

        if (exception instanceof LoginException) {
            sendErrorResponse(response,
                    ((LoginException) exception).getErrorMessage(),
                    ((LoginException) exception).getCode());
            return;
        }

        sendErrorResponse(response,
                LOGIN_FAILED.getErrorMessage(),
                LOGIN_FAILED.getCode());
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   String errorMessage,
                                   int code) throws IOException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");

        ObjectMapper mapper = new ObjectMapper();
        HashMap<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorMessage", errorMessage);
        errorResponse.put("code", code);

        String jsonResponse = mapper.writeValueAsString(errorResponse);
        response.getWriter().print(jsonResponse);
    }
}
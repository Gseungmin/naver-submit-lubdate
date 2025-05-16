package com.example.naver.web.exception.member;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class LoginException extends AuthenticationException {

    private final int code;
    private final String errorMessage;

    public LoginException(int code, String errorMessage) {
        super("");
        this.code = code;
        this.errorMessage = errorMessage;
    }
}

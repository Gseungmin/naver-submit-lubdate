package com.example.naver.web.exception.infra;

import lombok.Getter;


@Getter
public class InfraException extends RuntimeException {
    private final int code;
    private final String errorMessage;

    public InfraException(int code, String errorMessage){
        this.code = code;
        this.errorMessage = errorMessage;
    }
}

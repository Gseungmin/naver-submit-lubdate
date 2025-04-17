package com.example.naver.web.exception;

import lombok.Getter;


@Getter
public class CommonException extends RuntimeException {
    private final int code;
    private final String errorMessage;

    public CommonException(int code, String errorMessage){
        this.code = code;
        this.errorMessage = errorMessage;
    }
}

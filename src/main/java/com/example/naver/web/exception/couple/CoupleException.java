package com.example.naver.web.exception.couple;

import lombok.Getter;


@Getter
public class CoupleException extends RuntimeException {
    private final int code;
    private final String errorMessage;

    public CoupleException(int code, String errorMessage){
        this.code = code;
        this.errorMessage = errorMessage;
    }
}

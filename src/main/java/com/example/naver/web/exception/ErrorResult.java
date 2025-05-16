package com.example.naver.web.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResult {

    private int code;
    private String errorMessage;
}
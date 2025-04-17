package com.example.naver.web.exception.story;

import lombok.Getter;


@Getter
public class StoryException extends RuntimeException {
    private final int code;
    private final String errorMessage;

    public StoryException(int code, String errorMessage){
        this.code = code;
        this.errorMessage = errorMessage;
    }
}

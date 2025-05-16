package com.example.naver.web.exception.handler;

import com.example.naver.web.exception.CommonException;
import com.example.naver.web.exception.ErrorResult;
import com.example.naver.web.exception.couple.CoupleException;
import com.example.naver.web.exception.infra.InfraException;
import com.example.naver.web.exception.member.MemberException;
import com.example.naver.web.exception.story.StoryException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MemberException.class)
    public ErrorResult memberExceptionHandle(MemberException e, HttpServletRequest request) {
        log.error("[MemberException] url: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getMessage(), e.getCause());
        return new ErrorResult(e.getCode(), e.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CommonException.class)
    public ErrorResult commonExceptionHandle(CommonException e, HttpServletRequest request) {
        log.error("[CommonException] url: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getMessage(), e.getCause());
        return new ErrorResult(e.getCode(), e.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CoupleException.class)
    public ErrorResult coupleExceptionHandle(CoupleException e, HttpServletRequest request) {
        log.error("[CoupleException] url: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getMessage(), e.getCause());
        return new ErrorResult(e.getCode(), e.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(StoryException.class)
    public ErrorResult storyExceptionHandle(StoryException e, HttpServletRequest request) {
        log.error("[StoryException] url: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getMessage(), e.getCause());
        return new ErrorResult(e.getCode(), e.getErrorMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InfraException.class)
    public ErrorResult redisExceptionHandle(InfraException e, HttpServletRequest request) {
        log.error("[InfraException] url: {} | errorMessage: {} | cause Exception: ",
                request.getRequestURL(), e.getMessage(), e.getCause());
        return new ErrorResult(e.getCode(), e.getErrorMessage());
    }
}

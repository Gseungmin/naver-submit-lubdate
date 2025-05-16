package com.example.naver.web.util;
import com.example.naver.domain.dto.story.req.StoryCreateListRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateRequestDto;
import com.example.naver.domain.dto.story.req.StoryDeleteRequestDto;
import com.example.naver.domain.dto.story.req.StoryUpdateRequestDto;
import com.example.naver.web.exception.story.StoryException;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.*;


public class StoryValidator {
    public static void validateStoryListRequestDto(StoryCreateListRequestDto dto) {
        validateId(dto.getCoupleId());

        if (dto.getStoryList().isEmpty() || dto.getStoryList().size() > MAX_STORY_INSERT_LIMIT) {
            throw new StoryException(STORY_DTO_INVALID.getCode(), STORY_DTO_INVALID.getErrorMessage());
        }

        dto.getStoryList().forEach(item ->
        {
            validateUrl(item.getUrl());
            validateMemo(item.getMemo());
            validateLocation(item.getLocation());
            validateDate(item.getDate());
        });
    }

    public static void validateStoryCreateRequestDto(StoryCreateRequestDto dto) {
        validateId(dto.getCoupleId());
        validateUrl(dto.getUrl());
        validateMemo(dto.getMemo());
        validateLocation(dto.getLocation());
        validateDate(dto.getDate());
    }

    public static void validateStoryUpdateRequestDto(StoryUpdateRequestDto dto) {
        validateId(dto.getStoryId());
        validateId(dto.getCoupleId());
        validateMemo(dto.getMemo());
        validateLocation(dto.getLocation());
        validateDate(dto.getDate());
    }

    public static void validateStoryDeleteRequestDto(StoryDeleteRequestDto dto) {
        if (dto.getStoryIds() == null || dto.getStoryIds().isEmpty() || dto.getStoryIds().size() > MAX_STORY_DELETE_COUNT) {
            throw new StoryException(STORY_DTO_INVALID.getCode(), STORY_DTO_INVALID.getErrorMessage());
        }

        dto.getStoryIds().forEach(StoryValidator::validateId);
        validateId(dto.getCoupleId());
        validateId(dto.getOtherId());
    }

    public static void validateId(Long id) {
        if (id == null) {
            throw new StoryException(STORY_DTO_INVALID.getCode(), STORY_DTO_INVALID.getErrorMessage());
        }
    }

    private static void validateUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new StoryException(STORY_DTO_INVALID_URL.getCode(), STORY_DTO_INVALID_URL.getErrorMessage());
        }
    }

    private static void validateMemo(String content) {
        if (content == null || content.length() > MAX_STORY_MEMO_LENGTH) {
            throw new StoryException(STORY_DTO_INVALID_MEMO.getCode(), STORY_DTO_INVALID_MEMO.getErrorMessage());
        }
    }

    private static void validateLocation(String content) {
        if (content == null || content.length() > MAX_STORY_LOCATION_LENGTH) {
            throw new StoryException(STORY_DTO_INVALID_LOCATION.getCode(), STORY_DTO_INVALID_LOCATION.getErrorMessage());
        }
    }

    private static void validateDate(String content) {
        if (content == null || content.length() != MAX_STORY_DATE_LENGTH) {
            throw new StoryException(STORY_DTO_INVALID.getCode(), STORY_DTO_INVALID.getErrorMessage());
        }
    }
}

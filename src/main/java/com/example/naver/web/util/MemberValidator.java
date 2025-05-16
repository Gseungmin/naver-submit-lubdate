package com.example.naver.web.util;

import com.example.naver.domain.dto.member.req.MemberCreateRequestDto;
import com.example.naver.web.exception.ExceptionType;
import com.example.naver.web.exception.member.MemberException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.Objects;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.MAX_MEMBER_NAME_LIMIT;
import static com.example.naver.web.util.Util.MAX_MEMBER_PHONE_LIMIT;

public class MemberValidator {

    public static void validateAuthentication(Authentication authentication, HttpServletRequest request) {
        if (authentication == null) {
            ExceptionType exception = (ExceptionType) request.getAttribute("exception");
            throw new MemberException(exception.getCode(), exception.getErrorMessage());
        }

        if (null == authentication.getName()) {
            throw new MemberException(TOKEN_INVALID.getCode(), TOKEN_INVALID.getErrorMessage());
        }
    }

    public static void validateCreateMemberDto(MemberCreateRequestDto dto) {
        validateName(dto.getNickname());
        validateDate(dto.getBirth());
        validatePhone(dto.getPhone());
        validateToken(dto.getAccessToken());
    }

    public static void validateToken(String token) {
        if (token == null || Objects.equals(token, "")) {
            throw new MemberException(MEMBER_DTO_INVALID.getCode(), MEMBER_DTO_INVALID.getErrorMessage());
        }
    }

    public static void validateName(String name) {
        if (name == null || name.isEmpty() || name.length() > MAX_MEMBER_NAME_LIMIT) {
            throw new MemberException(MEMBER_DTO_INVALID_NAME.getCode(), MEMBER_DTO_INVALID_NAME.getErrorMessage());
        }
    }

    public static void validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            throw new MemberException(MEMBER_DTO_INVALID_PHONE.getCode(), MEMBER_DTO_INVALID_PHONE.getErrorMessage());
        }

        if (phone.trim().length() != MAX_MEMBER_PHONE_LIMIT) {
            throw new MemberException(MEMBER_DTO_INVALID_PHONE.getCode(), MEMBER_DTO_INVALID_PHONE.getErrorMessage());
        }

        if (!phone.matches("^(010|999)\\d{8}$")) {
            throw new MemberException(MEMBER_DTO_INVALID_PHONE.getCode(), MEMBER_DTO_INVALID_PHONE.getErrorMessage());
        }
    }

    public static void validateDate(LocalDate date) {
        if (date == null) {
            throw new MemberException(MEMBER_DTO_INVALID_DATE.getCode(), MEMBER_DTO_INVALID_DATE.getErrorMessage());
        }

        LocalDate currentDate = LocalDate.now();
        if (date.isAfter(currentDate)) {
            throw new MemberException(MEMBER_DTO_INVALID_DATE_AFTER.getCode(), MEMBER_DTO_INVALID_DATE_AFTER.getErrorMessage());
        }
    }
}

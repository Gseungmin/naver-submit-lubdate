package com.example.naver.web.util;

import com.example.naver.web.exception.member.MemberException;
import jakarta.servlet.http.HttpServletRequest;

import static com.example.naver.web.exception.ExceptionType.MEMBER_NOT_EXIST;

public class FunctionUtil {

    public static String getToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index).trim();
            }
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    public static Long parseLong(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new MemberException(
                    MEMBER_NOT_EXIST.getCode(),
                    MEMBER_NOT_EXIST.getErrorMessage()
            );
        }
    }
}

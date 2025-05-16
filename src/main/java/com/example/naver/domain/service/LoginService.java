package com.example.naver.domain.service;

import com.example.naver.domain.dto.member.res.TokenResponseDto;
import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.redis.login.LoginRedisService;
import com.example.naver.web.exception.member.MemberException;
import com.example.naver.web.signature.JWTSigner;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Objects;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.*;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final JWTSigner jwtSigner;
    private final LoginRedisService loginRedisService;
    private final RestTemplate restTemplate;

    public void loginCheck(String token, String userId, String prefix) {
        if (token == null) {
            throw new MemberException(
                    TOKEN_INVALID.getCode(),
                    TOKEN_INVALID.getErrorMessage()
            );
        }

        String tokenByPid = loginRedisService.findTokenByPid(prefix, userId);

        if (!Objects.equals(tokenByPid, token)) {
            throw new MemberException(
                    MULTI_LOGIN.getCode(),
                    MULTI_LOGIN.getErrorMessage()
            );
        }
    }

    public TokenResponseDto createToken(Member member) {
        String accessToken  = jwtSigner.getJwtToken(member, ACCESS_TOKEN_EXPIRED);
        String refreshToken = jwtSigner.getJwtToken(member, REFRESH_TOKEN_EXPIRED);

        loginRedisService.saveToken(member.getId().toString(), accessToken, refreshToken);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    public TokenResponseDto createToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        String accessToken  = jwtSigner.getJwtToken(user, ACCESS_TOKEN_EXPIRED);
        String refreshToken = jwtSigner.getJwtToken(user, REFRESH_TOKEN_EXPIRED);

        loginRedisService.saveToken(user.getUsername(), accessToken, refreshToken);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    public String accessToKakao(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_API,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonElement element = JsonParser.parseString(
                    Objects.requireNonNull(response.getBody())
            );
            return element.getAsJsonObject().get("id").toString();
        } catch (RestClientException e) {
            throw new MemberException(
                    SOCIAL_CONNECT_FAILED.getCode(),
                    SOCIAL_CONNECT_FAILED.getErrorMessage()
            );
        }
    }
}

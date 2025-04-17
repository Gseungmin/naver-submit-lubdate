package com.example.naver.domain.service;

import com.example.naver.domain.dto.member.res.TokenResponseDto;
import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.redisService.LoginRedisService;
import com.example.naver.web.exception.member.MemberException;
import com.example.naver.web.filter.apple.ApplePublicKeyGenerator;
import com.example.naver.web.filter.apple.ApplePublicKeyResponse;
import com.example.naver.web.signature.JWTSigner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.Util.*;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final JWTSigner jwtSigner;
    private final LoginRedisService loginRedisService;
    private final RestTemplate restTemplate;
    private final ApplePublicKeyGenerator applePublicKeyGenerator;

    public void loginCheck(String token, String userId, String prefix) {
        if (token == null) {
            throw new MemberException(TOKEN_INVALID.getCode(), TOKEN_INVALID.getErrorMessage());
        }

        String tokenByPid = loginRedisService.findTokenByPid(prefix, userId);

        if (!Objects.equals(tokenByPid, token)) {
            throw new MemberException(MULTI_LOGIN.getCode(), MULTI_LOGIN.getErrorMessage());
        }
    }

    public TokenResponseDto createToken(Member member) {
        String accessToken = jwtSigner.getJwtToken(member, ACCESS_TOKEN_EXPIRED);
        String refreshToken = jwtSigner.getJwtToken(member, REFRESH_TOKEN_EXPIRED);
        loginRedisService.saveToken(member.getId().toString(), accessToken, refreshToken);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    public TokenResponseDto createToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtSigner.getJwtToken(user, ACCESS_TOKEN_EXPIRED);
        String refreshToken = jwtSigner.getJwtToken(user, REFRESH_TOKEN_EXPIRED);
        loginRedisService.saveToken(user.getUsername(), accessToken, refreshToken);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    public String accessToKakao(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(KAKAO_API, HttpMethod.POST, entity, String.class);

            JsonElement element = JsonParser.parseString(Objects.requireNonNull(response.getBody()));
            return element.getAsJsonObject().get("id").toString();
        } catch (RestClientException e) {
            throw new MemberException(SOCIAL_CONNECT_FAILED.getCode(), SOCIAL_CONNECT_FAILED.getErrorMessage());
        }
    }

    public void accessToApple(String idToken, String User) {
        try {
            Map<String, String> tokenHeaders = parseHeaders(idToken);
            ApplePublicKeyResponse appleKeys = restTemplate.getForObject(APPLE_API, ApplePublicKeyResponse.class);

            PublicKey publicKey = applePublicKeyGenerator.generatePublicKey(tokenHeaders, appleKeys);
            Claims claims = getTokenClaims(idToken, publicKey);

            if (Objects.equals(User, claims.getSubject())) {
                return;
            }

            throw new MemberException(SOCIAL_CONNECT_FAILED.getCode(), SOCIAL_CONNECT_FAILED.getErrorMessage());
        } catch (Exception e) {
            throw new MemberException(SOCIAL_CONNECT_FAILED.getCode(), SOCIAL_CONNECT_FAILED.getErrorMessage());
        }
    }

    public Map<String, String> parseHeaders(String token) throws JsonProcessingException {
        String header = token.split("\\.")[0];
        return new ObjectMapper().readValue(decodeHeader(header), Map.class);
    }


    public Claims getTokenClaims(String token, PublicKey publicKey) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String decodeHeader(String token) {
        return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
    }
}
package com.example.naver.web.filter.authentication;

import com.example.naver.domain.dto.member.res.TokenResponseDto;
import com.example.naver.domain.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class JwtKakaoAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final HttpSecurity httpSecurity;
    private final LoginService loginService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        String accessToken = request.getHeader("Authorization");
        String socialId    = loginService.accessToKakao(accessToken);

        return authenticateUser(socialId);
    }

    private Authentication authenticateUser(String socialId) {
        AuthenticationManager authenticationManager =
                httpSecurity.getSharedObject(AuthenticationManager.class);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(socialId, "KAKAO");

        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        TokenResponseDto dto = loginService.createToken(authResult);

        response.setContentType("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(dto);
        response.getWriter().write(jsonString);
    }
}

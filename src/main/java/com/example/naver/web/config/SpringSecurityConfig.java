package com.example.naver.web.config;

import com.example.naver.domain.service.LoginService;
import com.example.naver.web.filter.authentication.CustomUserDetailsService;
import com.example.naver.web.filter.authentication.JwtKakaoAuthenticationFilter;
import com.example.naver.web.filter.authorization.JwtAuthorizationRsaFilter;
import com.example.naver.web.filter.exception.CustomAuthenticationEntryPoint;
import com.example.naver.web.filter.exception.CustomAuthenticationFailureHandler;
import com.example.naver.web.filter.security.BlacklistFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig {

    private final CustomUserDetailsService       userDetailsService;
    private final CustomAuthenticationFailureHandler authFailureHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final LoginService                   loginService;
    private final BlacklistFilter                blacklistFilter;
    private final JwtAuthorizationRsaFilter      jwtAuthorizationRsaFilter;

    private String[] permitAllUrlPatterns() {
        return new String[] {
                "/",
                "/auth/kakao",
                "/auth/apple",
                "/auth/phone",
                "/auth/logout",
                "/auth/blacklist"
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(requests ->
                        requests.requestMatchers(permitAllUrlPatterns())
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .exceptionHandling(handler ->
                        handler.authenticationEntryPoint(authenticationEntryPoint));

        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.userDetailsService(userDetailsService);

        JwtKakaoAuthenticationFilter jwtKakaoAuthenticationFilter =
                new JwtKakaoAuthenticationFilter(http, loginService);

        jwtKakaoAuthenticationFilter.setAuthenticationFailureHandler(authFailureHandler);
        jwtKakaoAuthenticationFilter.setFilterProcessesUrl("/auth/login/kakao");

        http.addFilter(jwtKakaoAuthenticationFilter)
                .addFilterBefore(jwtAuthorizationRsaFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(blacklistFilter, CorsFilter.class);

        return http.build();
    }
}

package com.example.naver.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public Key jwtSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }
}

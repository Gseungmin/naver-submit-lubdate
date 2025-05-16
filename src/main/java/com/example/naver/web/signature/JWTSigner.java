package com.example.naver.web.signature;

import com.example.naver.domain.entity.member.Member;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JWTSigner {

    private final Key key;

    public String getJwtToken(User user, Long time) {
        List<String> authority = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setExpiration(new Date(new Date().getTime() + time))
                .claim("identifier", UUID.randomUUID().toString())
                .claim("role", authority)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getJwtToken(Member member, Long time) {
        List<String> authority = member.getRoles().stream()
                .map(role -> "ROLE_" + role)
                .toList();

        return Jwts.builder()
                .setSubject(member.getId().toString())
                .setExpiration(new Date(new Date().getTime() + time))
                .claim("identifier", UUID.randomUUID().toString())
                .claim("role", authority)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}


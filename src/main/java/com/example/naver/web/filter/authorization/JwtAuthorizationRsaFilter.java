package com.example.naver.web.filter.authorization;

import com.example.naver.domain.service.LoginService;
import com.example.naver.web.exception.ExceptionType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.naver.web.exception.ExceptionType.*;
import static com.example.naver.web.util.FunctionUtil.getToken;
import static com.example.naver.web.util.Util.ACCESS_TOKEN;
import static com.example.naver.web.util.Util.POSSIBLE_GET_ROUTE;


@Component
@RequiredArgsConstructor
public class JwtAuthorizationRsaFilter extends OncePerRequestFilter {

    private final Key key;
    private final LoginService loginService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        request.getMethod();

        AntPathMatcher pathMatcher = new AntPathMatcher();
        return pathMatcher.match("/", path)
                || pathMatcher.match("/auth/apple", path)
                || pathMatcher.match("/auth/kakao", path)
                || pathMatcher.match("/auth/logout", path)
                || pathMatcher.match("/auth/phone", path)
                || pathMatcher.match("/auth/blacklist", path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        try {
            String token = getToken(request);

            if (token == null) {
                setException(request, TOKEN_NOT_EXIST, chain, response);
                return;
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (shouldCheckLogin(request)) {
                loginService.loginCheck(token, claims.getSubject(), ACCESS_TOKEN);
            }

            createAuthentication(request, claims);
        } catch (ExpiredJwtException e) {
            setException(request, TOKEN_EXPIRED, chain, response);
            return;
        } catch (Exception e) {
            setException(request, TOKEN_INVALID, chain, response);
            return;
        }

        chain.doFilter(request, response);
    }

    private void setException(HttpServletRequest request,
                              ExceptionType errorType,
                              FilterChain chain,
                              HttpServletResponse response) throws IOException, ServletException {
        request.setAttribute("exception", errorType);
        chain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private void createAuthentication(HttpServletRequest request, Claims claims) {
        String pid = claims.getSubject();
        List<String> roles = (List<String>) claims.get("role");

        Set<GrantedAuthority> grantedAuthorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        if (pid != null && !grantedAuthorities.isEmpty()) {
            UserDetails user = User.builder()
                    .username(pid)
                    .password(UUID.randomUUID().toString())
                    .authorities(grantedAuthorities)
                    .build();

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            request.setAttribute("exception", TOKEN_INVALID);
        }
    }

    private boolean shouldCheckLogin(HttpServletRequest request) {
        if (request.getRequestURI().equals("/auth/reissue")) {
            return false;
        }

        if (!request.getMethod().equalsIgnoreCase("GET")) {
            return true;
        }

        return POSSIBLE_GET_ROUTE.contains(request.getRequestURI());
    }
}


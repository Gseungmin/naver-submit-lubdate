package com.example.naver.domain.redis.login;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LoginRedisService {

    private final RedisTemplate<String, String> stringTemplate;

    public void saveToken(String username, String accessToken, String refreshToken) {
        stringTemplate.executePipelined(new SessionCallback<Void>() {
            @Override
            public Void execute(RedisOperations operations) throws DataAccessException {
                ValueOperations<String, String> valueOps = operations.opsForValue();
                valueOps.set(username + ":accessToken", accessToken, 30, TimeUnit.DAYS);
                valueOps.set(username + ":refreshToken", refreshToken, 60, TimeUnit.DAYS);
                return null;
            }
        });
    }

    public String findTokenByPid(String prefix, String username) {
        return stringTemplate.opsForValue().get(username + ":" + prefix);
    }
}

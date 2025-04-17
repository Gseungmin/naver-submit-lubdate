package com.example.naver.domain.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlacklistService {
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();
    private static final long BLOCK_DURATION = 10 * 60 * 1000;

    public void addToBlacklist(String ip) {
        if (Objects.equals(ip, "0:0:0:0:0:0:0:1") || Objects.equals(ip, "127.0.0.1")) {
            return;
        }

        blacklist.put(ip, System.currentTimeMillis() + BLOCK_DURATION);
    }

    public boolean isBlacklisted(String ip) {
        Long expiryTime = blacklist.get(ip);

        if (expiryTime == null) {
            return false;
        }

        if (System.currentTimeMillis() > expiryTime) {
            blacklist.remove(ip);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 60 * 1000 * 10)
    private void cleanupBlacklist() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}


package com.example.naver.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

@Service
@RequiredArgsConstructor
public class CoupleLockService {

    public final ConcurrentHashMap<Long, Lock> storyLocks = new ConcurrentHashMap<>();

    public void cleanupStoryLock(Long id, Lock lock) {
        storyLocks.remove(id, lock);
    }

    public void cleanAll() {
        storyLocks.clear();
    }
}

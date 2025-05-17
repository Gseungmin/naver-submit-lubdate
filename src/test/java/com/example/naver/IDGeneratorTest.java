package com.example.naver;

import com.example.naver.domain.generator.CodeGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class IDGeneratorTest {

    @Autowired
    private CodeGenerator codeGenerator;

    @Test
    @DisplayName("동시성에 1000개의 스레드가 요청을 해도 아이디가 중복되지 않는다는 것을 검증할 수 있다.")
    public void 동시성_검증() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        Set<Long> idSet = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                long id = codeGenerator.generateId();
                idSet.add(id);
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        assertEquals(threadCount, idSet.size());
    }
}
package com.example.naver.domain.generator;

import com.example.naver.web.exception.CommonException;
import org.springframework.stereotype.Component;

import static com.example.naver.web.exception.ExceptionType.SYSTEM_TIME_EXCEPTION;
import static com.example.naver.web.exception.ExceptionType.UNKNOWN_ERROR;
import static com.example.naver.web.util.Util.EPOCH;

@Component
public class CodeGenerator {

    private static final long SEQUENCE_BITS   = 12L;
    private static final long MAX_SEQUENCE    = ~(-1L << SEQUENCE_BITS);

    private long sequence      = 0L;
    private long lastTimestamp = -1L;

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private long waitForNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    public synchronized long generateId() {
        long timestamp = currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new CommonException(
                    SYSTEM_TIME_EXCEPTION.getCode(),
                    SYSTEM_TIME_EXCEPTION.getErrorMessage()
            );
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << SEQUENCE_BITS)
                | sequence;
    }

    public String generateBase36Id() {
        long id = generateId();
        return encodeBase36(id);
    }

    public String encodeBase36(long id) {
        final String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            int index = (int)(id % 36);
            sb.append(characters.charAt(index));
            id /= 36;
        }
        return sb.reverse().toString();
    }
}
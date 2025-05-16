package com.example.naver.domain.generator;

import com.example.naver.web.exception.CommonException;
import org.springframework.stereotype.Component;

import static com.example.naver.web.exception.ExceptionType.UNKNOWN_ERROR;
import static com.example.naver.web.util.Util.EPOCH;

@Component
public class CodeGenerator {

    private final long epoch = EPOCH;
    private final long sequenceBits = 12L;
    private final long maxSequence = ~(-1L << sequenceBits);
    private final long timestampLeftShift = sequenceBits;
    private long sequence = 0L;
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
            throw new CommonException(UNKNOWN_ERROR.getCode(), UNKNOWN_ERROR.getErrorMessage());
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = waitForNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - epoch) << timestampLeftShift) | sequence;
    }

    public String generateBase64Id() {
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
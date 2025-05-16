package com.example.naver;

import com.example.naver.web.security.CryptoUtil;
import com.example.naver.web.security.EncryptorConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EncryptorConverterTest {

    @Autowired
    private CryptoUtil cryptoUtil;
    private EncryptorConverter converter;

    @BeforeEach
    void setUp() {
        converter = new EncryptorConverter(cryptoUtil);
    }

    @Test
    @DisplayName("IV가 같으면 암호화 및 복호화의 값이 같다")
    void 암호화_복호화의_값이_같다() {
        for (int i = 0; i < 100; i++) {
            String original  = UUID.randomUUID().toString();
            String encrypted = converter.convertToDatabaseColumn(original);
            String decrypted = converter.convertToEntityAttribute(encrypted);

            // 1️⃣ 암호문은 평문과 달라야 하고
            assertThat(encrypted).isNotEqualTo(original);

            // 2️⃣ 복호화 결과는 원본과 같아야 한다
            assertThat(decrypted).isEqualTo(original);
        }
    }
}
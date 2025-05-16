package com.example.naver.web.security;

import com.example.naver.web.exception.CommonException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static com.example.naver.web.exception.ExceptionType.DECRYPT_FAIL;
import static com.example.naver.web.exception.ExceptionType.ENCRYPT_FAIL;

@Component
@RequiredArgsConstructor
public class CryptoUtil {

    private static String SECRET_KEY;

    private static final int GCM_IV_LENGTH  = 12; // 96 bit
    private static final int GCM_TAG_LENGTH = 16; // 128 bit

    @Value("${crypto.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        SECRET_KEY = secretKey;
    }

    /* ENCRYPT (AES-GCM) */
    public String encrypt(String plain) {
        try {
            // 1️⃣ 난수 IV 생성 (반드시 매 번 달라야 함)
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            SecretKeySpec key   = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            // 2️⃣ Cipher 초기화 & 암호화 수행
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            // 3️⃣ [IV ‖ CIPHER]로 붙여서 인코딩
            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv).put(cipherText);
            return Base64.getEncoder().encodeToString(bb.array());

        } catch (Exception ex) {
            throw new CommonException(
                    ENCRYPT_FAIL.getCode(),
                    ENCRYPT_FAIL.getErrorMessage()
            );
        }
    }

    /* DECRYPT (AES-GCM) */
    public String decrypt(String encoded) {
        try {
            // 1️⃣ Base64 디코딩
            byte[] input = Base64.getDecoder().decode(encoded);

            // 2️⃣ 앞쪽 12바이트 → IV, 나머지 → [CIPHER‖TAG]
            ByteBuffer bb = ByteBuffer.wrap(input);
            byte[] iv = new byte[GCM_IV_LENGTH];
            bb.get(iv);
            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);

            SecretKeySpec key   = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            // 3️⃣ Cipher 초기화 & 복호화 수행
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            // 4️⃣ UTF‑8 문자열 변환 후 반환
            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);

        } catch (Exception ex) {
            throw new CommonException(
                    DECRYPT_FAIL.getCode(),
                    DECRYPT_FAIL.getErrorMessage()
            );
        }
    }
}

package com.example.naver.web.security;

import com.example.naver.web.exception.CommonException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static com.example.naver.web.exception.ExceptionType.ENCRYPT_FAIL;

@Component
@RequiredArgsConstructor
public class CryptoUtil {

    private static String SECRET_KEY;
    private static String INIT_VECTOR;

    @Value("${crypto.secret-key}")
    private String secretKey;

    @Value("${crypto.init-vector}")
    private String initVector;

    @PostConstruct
    public void init() {
        SECRET_KEY = secretKey;
        INIT_VECTOR = initVector;
    }

    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new CommonException(ENCRYPT_FAIL.getCode(), ENCRYPT_FAIL.getErrorMessage());
        }
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original);
        } catch (Exception ex) {
            throw new CommonException(ENCRYPT_FAIL.getCode(), ENCRYPT_FAIL.getErrorMessage());
        }
    }
}
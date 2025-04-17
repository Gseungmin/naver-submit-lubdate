package com.example.naver.web.filter.apple;

import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

@Component
public class ApplePublicKeyGenerator {
    public PublicKey generatePublicKey(Map<String, String> tokenHeaders, ApplePublicKeyResponse applePublicKeys)
            throws AuthenticationException, NoSuchAlgorithmException, InvalidKeySpecException {
        ApplePublicKey publicKey = applePublicKeys.getMatchedKey(tokenHeaders.get("kid"), tokenHeaders.get("alg"));

        byte[] nBytes = Base64.getUrlDecoder().decode(publicKey.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(publicKey.getE());

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(1, nBytes), new BigInteger(1, eBytes));
        KeyFactory keyFactory = KeyFactory.getInstance(publicKey.getKty());

        return keyFactory.generatePublic(publicKeySpec);
    }
}

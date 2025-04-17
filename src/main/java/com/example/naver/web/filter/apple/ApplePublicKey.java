package com.example.naver.web.filter.apple;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class ApplePublicKey {
    private String kty;
    private String kid;
    private String alg;
    private String n;
    private String e;
}

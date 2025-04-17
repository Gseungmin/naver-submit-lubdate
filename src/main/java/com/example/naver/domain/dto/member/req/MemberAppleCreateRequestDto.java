package com.example.naver.domain.dto.member.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class MemberAppleCreateRequestDto {

    private String idToken;
    private String user;
    private String phone;
    private String nickname;
    private LocalDate birth;
}

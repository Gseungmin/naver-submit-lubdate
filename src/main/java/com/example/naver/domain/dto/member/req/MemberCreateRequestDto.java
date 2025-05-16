package com.example.naver.domain.dto.member.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@AllArgsConstructor
public class MemberCreateRequestDto {

    private String accessToken;
    private String phone;
    private String nickname;
    private LocalDate birth;
}

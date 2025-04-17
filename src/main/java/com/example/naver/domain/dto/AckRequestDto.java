package com.example.naver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class AckRequestDto {

    private Long sequenceNumber;

    public AckRequestDto() {
    }
}

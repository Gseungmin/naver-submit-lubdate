package com.example.naver.domain.dto.queue.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class AckRequestDto {

    private Long sequenceNumber;
}

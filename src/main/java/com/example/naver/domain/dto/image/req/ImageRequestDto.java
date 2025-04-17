package com.example.naver.domain.dto.image.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ImageRequestDto {

    private String filename;
    private String type;
}

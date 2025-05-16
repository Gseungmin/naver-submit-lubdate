package com.example.naver.domain.dto.story.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StoryCreateRequestDto {

    private Long coupleId;
    private String url;
    private String memo;
    private String date;
    private String location;
    private String fcm;
}

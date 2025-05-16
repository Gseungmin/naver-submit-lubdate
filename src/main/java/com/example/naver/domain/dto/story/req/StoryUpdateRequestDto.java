package com.example.naver.domain.dto.story.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StoryUpdateRequestDto {

    private Long otherId;
    private Long storyId;
    private Long coupleId;
    private String memo;
    private String date;
    private String location;
}

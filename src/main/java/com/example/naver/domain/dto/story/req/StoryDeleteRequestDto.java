package com.example.naver.domain.dto.story.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class StoryDeleteRequestDto {

    private List<Long> storyIds;
    private Long coupleId;
    private Long otherId;
}

package com.example.naver.domain.dto.story.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoryCreateListRequestDto {

    private Long coupleId;
    private String fcm;
    private List<StoryCreateListItemRequestDto> storyList;
}

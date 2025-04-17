package com.example.naver.domain.dto.story.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoryCreateListItemRequestDto {

    private String url;
    private String memo;
    private String date;
    private String location;
}

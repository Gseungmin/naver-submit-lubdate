package com.example.naver.domain.dto.story.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoryListItemResponseDto {

    private List<StoryItemResponseDto> stories;
}

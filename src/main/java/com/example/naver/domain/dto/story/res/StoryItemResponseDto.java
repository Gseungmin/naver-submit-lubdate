package com.example.naver.domain.dto.story.res;

import com.example.naver.domain.dto.story.req.StoryUpdateRequestDto;
import com.example.naver.domain.entity.story.Story;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class StoryItemResponseDto {

    private String storyId;
    private String storyIds;
    private String url;
    private String memo;
    private String location;
    private String date;
    private Boolean status;
    private String createdDate;
    private String creator;
    private String lastModifiedDate;

    public StoryItemResponseDto(Story story) {
        this.storyId = story.getId().toString();
        this.url = story.getUrl();
        this.memo = story.getMemo();
        this.location = story.getLocation();
        this.date = story.getDate();
        this.status = story.getStatus();
        this.createdDate = story.getCreatedDate().toString();
        this.creator = story.getMemberId().toString();
        this.lastModifiedDate = story.getLastModifiedDate().toString();
    }

    public StoryItemResponseDto(Story story, StoryUpdateRequestDto dto) {
        this.storyId = story.getId().toString();
        this.url = story.getUrl();
        this.memo = dto.getMemo();
        this.location = dto.getLocation();
        this.date = dto.getDate();
        this.status = story.getStatus();
        this.createdDate = story.getCreatedDate().toString();
        this.creator = story.getMemberId().toString();
        this.lastModifiedDate = LocalDateTime.now().toString();
    }

    public StoryItemResponseDto(StoryItemResponseDto data) {
        this.storyId = data.getStoryId();
        this.url = data.getUrl();
        this.memo = data.getMemo();
        this.location = data.getLocation();
        this.date = data.getDate();
        this.status = data.getStatus();
        this.createdDate = data.getCreatedDate();
        this.creator = data.getCreator();
        this.lastModifiedDate = data.getLastModifiedDate();
    }

    public StoryItemResponseDto(List<String> storyIds) {
        this.storyId = storyIds.get(0);
        this.storyIds = String.join(",", storyIds);
    }
}

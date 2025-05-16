package com.example.naver.domain.entity.story;

import com.example.naver.domain.dto.story.req.StoryCreateListItemRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateRequestDto;
import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.entity.base.BaseEntity;
import com.example.naver.domain.generator.SnowflakeId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.example.naver.web.util.Util.ITEM_CREATED;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "story", indexes = {
        @Index(name = "idx_story_couple_identifier", columnList = "coupleIdentifier"),
        @Index(name = "idx_story_member_id", columnList = "memberId")
})
public class Story extends BaseEntity {

    @Id
    @GeneratedValue @SnowflakeId
    @Column(name = "storyId")
    private Long id;

//    @Convert(converter = EncryptorConverter.class)
    private String url;
    private String date;
    private String memo;
    private String location;

    private Boolean status = ITEM_CREATED;

    @Column(name = "coupleIdentifier")
    private Long coupleIdentifier;

    @Column(name = "memberId")
    private Long memberId;

    public Story(StoryCreateRequestDto dto, Long memberId) {
        this.memo = dto.getMemo();
        this.url = dto.getUrl();
        this.coupleIdentifier = dto.getCoupleId();
        this.memberId = memberId;
        this.date = dto.getDate();
        this.location = dto.getLocation();
    }

    public Story(Long id, StoryCreateListItemRequestDto dto, Long coupleId, Long memberId, LocalDateTime date) {
        this.id = id;
        this.memo = dto.getMemo();
        this.url = dto.getUrl();
        this.coupleIdentifier = coupleId;
        this.memberId = memberId;
        this.date = dto.getDate();
        this.location = dto.getLocation();
        this.setCreatedDate(date);
        this.setLastModifiedDate(date);
    }

    public Story(Long id, StoryCreateListItemRequestDto dto, Long coupleId, Long memberId, LocalDateTime date, String url) {
        this.id = id;
        this.memo = dto.getMemo();
        this.url = url;
        this.coupleIdentifier = coupleId;
        this.memberId = memberId;
        this.date = dto.getDate();
        this.location = dto.getLocation();
        this.setCreatedDate(date);
        this.setLastModifiedDate(date);
    }

    public void updateStory(StoryItemResponseDto data) {
        this.date = data.getDate();
        this.memo = data.getMemo();
        this.location = data.getLocation();
        this.setLastModifiedDate(LocalDateTime.now());
    }
}

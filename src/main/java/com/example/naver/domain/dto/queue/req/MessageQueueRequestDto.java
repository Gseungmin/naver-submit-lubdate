package com.example.naver.domain.dto.queue.req;
import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.dto.story.res.StoryListItemResponseDto;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.example.naver.web.util.Util.MESSAGE_STORY;
import static com.example.naver.web.util.Util.MESSAGE_STORY_BULK;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageQueueRequestDto {

    private String messageId;
    private String sender;
    private String status;
    private Long sequenceNumber;
    private String type;
    private String fcm;
    private String alarmId;
    private String createdDate;

    @JsonTypeInfo(
            use          = JsonTypeInfo.Id.NAME,
            include      = JsonTypeInfo.As.PROPERTY,
            property     = "type",
            defaultImpl  = StoryItemResponseDto.class
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StoryItemResponseDto.class, name = MESSAGE_STORY),
            @JsonSubTypes.Type(value = StoryListItemResponseDto.class, name = MESSAGE_STORY_BULK)
    })
    private Object message;

    public MessageQueueRequestDto(StoryListItemResponseDto data,
                                  Long senderId,
                                  String status) {

        this.messageId = data.getStories().get(0).getStoryId();
        this.sender = senderId.toString();
        this.message = data;
        this.status = status;
        this.type = MESSAGE_STORY_BULK;
    }

    public MessageQueueRequestDto(StoryItemResponseDto data,
                                  Long senderId,
                                  String status) {

        this.messageId = data.getStoryId();
        this.sender = senderId.toString();
        this.message = data;
        this.status = status;
        this.type = MESSAGE_STORY;
    }

    public MessageQueueRequestDto(String messageId,
                                  StoryItemResponseDto data,
                                  Long senderId,
                                  String status) {

        this.messageId = messageId;
        this.sender = senderId.toString();
        this.message = data;
        this.status = status;
        this.type = MESSAGE_STORY;
    }
}

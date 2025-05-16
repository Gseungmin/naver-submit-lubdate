package com.example.naver.web.controller;

import com.example.naver.domain.dto.story.req.StoryCreateListRequestDto;
import com.example.naver.domain.dto.story.req.StoryCreateRequestDto;
import com.example.naver.domain.dto.story.req.StoryDeleteRequestDto;
import com.example.naver.domain.dto.story.req.StoryUpdateRequestDto;
import com.example.naver.domain.dto.story.res.StoryItemResponseDto;
import com.example.naver.domain.dto.story.res.StoryListItemResponseDto;
import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.service.MemberService;
import com.example.naver.domain.service.StoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.example.naver.web.util.FunctionUtil.parseLong;
import static com.example.naver.web.util.MemberValidator.validateAuthentication;
import static com.example.naver.web.util.StoryValidator.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/story")
public class StoryController {

    private final MemberService memberService;
    private final StoryService storyService;

    /* 스토리 목록 조회 */
    @GetMapping("/list")
    public StoryListItemResponseDto getStoryList(Authentication authentication,
                                                 HttpServletRequest request,
                                                 @RequestParam("coupleId") Long coupleId) {

        validateAuthentication(authentication, request);
        validateId(coupleId);
        return storyService.getStoryListAfterUpdate(coupleId);
    }

    /* 스토리 등록 */
    @PostMapping
    public StoryItemResponseDto createStory(Authentication authentication,
                                            HttpServletRequest request,
                                            @RequestBody StoryCreateRequestDto dto) {

        validateAuthentication(authentication, request);
        validateStoryCreateRequestDto(dto);

        Long memberId = parseLong(authentication.getName());
        Member member = memberService.findByIdWithCouple(memberId);

        return storyService.createStory(member, dto);
    }

    /* 스토리 벌크 등록 */
    @PostMapping("/bulk")
    public StoryListItemResponseDto createBulkStory(Authentication authentication,
                                                    HttpServletRequest request,
                                                    @RequestBody StoryCreateListRequestDto dto) {

        validateAuthentication(authentication, request);
        validateStoryListRequestDto(dto);

        Long memberId = parseLong(authentication.getName());
        Member member = memberService.findByIdWithCouple(memberId);

        return storyService.createBulkStory(member, dto);
    }

    /* 스토리 업데이트 */
    @PatchMapping
    public StoryItemResponseDto updateStory(Authentication authentication,
                                            HttpServletRequest request,
                                            @RequestBody StoryUpdateRequestDto dto) {

        validateAuthentication(authentication, request);
        validateStoryUpdateRequestDto(dto);

        Long memberId = parseLong(authentication.getName());
        return storyService.updateStory(dto, memberId);
    }

    /* 스토리 삭제 */
    @DeleteMapping
    public void deleteStory(Authentication authentication,
                            HttpServletRequest request,
                            @RequestBody StoryDeleteRequestDto dto) {

        validateAuthentication(authentication, request);
        validateStoryDeleteRequestDto(dto);

        Long memberId = parseLong(authentication.getName());
        storyService.deleteStory(dto.getStoryIds(),
                dto.getOtherId(),
                memberId,
                dto.getCoupleId());
    }
}

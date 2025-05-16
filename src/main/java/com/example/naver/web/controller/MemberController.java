package com.example.naver.web.controller;

import com.example.naver.domain.dto.member.req.MemberCreateRequestDto;
import com.example.naver.domain.dto.member.res.TokenResponseDto;
import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.service.LoginService;
import com.example.naver.domain.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.example.naver.web.util.FunctionUtil.getToken;
import static com.example.naver.web.util.FunctionUtil.parseLong;
import static com.example.naver.web.util.MemberValidator.validateAuthentication;
import static com.example.naver.web.util.MemberValidator.validateCreateMemberDto;
import static com.example.naver.web.util.Util.REFRESH_TOKEN;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class MemberController {

    private final MemberService memberService;
    private final LoginService loginService;

    /*카카오 로그인*/
    @PostMapping("/login/kakao")
    public void kakaoLogin() {}

    /*애플 로그인*/
    @PostMapping("/login/apple")
    public void appleLogin() {}

    /*카카오 회원 가입*/
    @PostMapping("/kakao")
    public TokenResponseDto createMember(@RequestBody MemberCreateRequestDto dto) {
        validateCreateMemberDto(dto);

        String socialId = loginService.accessToKakao(dto.getAccessToken());
        Member member = memberService.createMember(dto, socialId);

        return loginService.createToken(member);
    }

    /* 토큰 재발급 */
    @GetMapping("/reissue")
    public TokenResponseDto reIssueToken(
            Authentication authentication,
            HttpServletRequest request
    ) {

        validateAuthentication(authentication, request);

        Long memberId = parseLong(authentication.getName());
        String token = getToken(request);

        loginService.loginCheck(token, memberId.toString(), REFRESH_TOKEN);
        return loginService.createToken(authentication);
    }
}

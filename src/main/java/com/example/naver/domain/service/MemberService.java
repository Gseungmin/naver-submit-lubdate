package com.example.naver.domain.service;

import com.example.naver.domain.dto.member.req.MemberCreateRequestDto;
import com.example.naver.domain.entity.member.Auth;
import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.generator.CodeGenerator;
import com.example.naver.domain.repository.AuthRepository;
import com.example.naver.domain.repository.MemberRepository;
import com.example.naver.web.exception.member.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.naver.web.exception.ExceptionType.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final CodeGenerator codeGenerator;
    private final AuthRepository authRepository;

    @Transactional(readOnly = true)
    public Member findByIdWithCouple(Long id) {
        Optional<Member> findMember = memberRepository.findByIdWithCouple(id);

        if (findMember.isEmpty()) {
            throw new MemberException(
                    MEMBER_NOT_EXIST.getCode(),
                    MEMBER_NOT_EXIST.getErrorMessage()
            );
        }

        Member member = findMember.get();

        if (member.getNickname() == null) {
            throw new MemberException(
                    DELETED_MEMBER.getCode(),
                    DELETED_MEMBER.getErrorMessage()
            );
        }

        return member;
    }


    /* 회원 번호 조회 */
    @Transactional(readOnly = true)
    public void findByPhone(String phoneNumber) {
        Optional<Auth> findAuth = authRepository.findByPhoneNumber(phoneNumber);

        if (findAuth.isPresent()) {
            Auth auth = findAuth.get();

            if (auth.getDeleteDate() != null) {
                throw new MemberException(
                        DELETED_PHONE_NUMBER.getCode(),
                        DELETED_PHONE_NUMBER.getErrorMessage()
                );
            }

            throw new MemberException(
                    PHONE_INFO_EXIST.getCode(),
                    PHONE_INFO_EXIST.getErrorMessage()
            );
        }
    }

    /* 회원 저장 - 카카오 */
    public Member createMember(MemberCreateRequestDto dto, String socialId) {
        findByPhone(dto.getPhone());

        Auth auth = new Auth(dto.getPhone());
        Member member = new Member(
                dto,
                codeGenerator.generateBase36Id(),
                socialId
        );

        member.addAuth(auth);
        return memberRepository.save(member);
    }
}

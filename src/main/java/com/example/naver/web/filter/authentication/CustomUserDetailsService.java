package com.example.naver.web.filter.authentication;

import com.example.naver.domain.entity.member.Member;
import com.example.naver.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.naver.web.exception.ExceptionType.LOGIN_FAILED;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String socialId) {
        return memberRepository.findBySocialId(socialId)
                .map(this::createUserDetails)
                .orElseThrow(() ->
                        new UsernameNotFoundException(LOGIN_FAILED.getErrorMessage()));
    }

    private UserDetails createUserDetails(Member member) {
        return User.builder()
                .username(member.getId().toString())
                .password(member.getSocialType())
                .roles(member.getRoles().toArray(new String[0]))
                .build();
    }
}

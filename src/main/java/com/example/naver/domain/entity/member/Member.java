package com.example.naver.domain.entity.member;

import com.example.naver.domain.dto.member.req.MemberAppleCreateRequestDto;
import com.example.naver.domain.dto.member.req.MemberCreateRequestDto;
import com.example.naver.domain.entity.base.BaseEntity;
import com.example.naver.web.security.EncryptorConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.example.naver.web.util.Util.DEFAULT_PROFILE_IMAGE;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(generator = "member_seq_id")
    @GenericGenerator(name = "member_seq_id", strategy = "com.example.naver.domain.generator.MemberIDGenerator")
    @Column(name = "member_id")
    private Long id;

    private String socialId;
    private String socialType;

    private String nickname;
    private LocalDate birth;
    private String gender;

    @Convert(converter = EncryptorConverter.class)
    private String profileImage;

    @Column(unique = true)
    private String code;
    private Long lastCoupleId;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> roles = new ArrayList<>();

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "authId")
    private Auth auth;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "coupleId")
    private Couple couple;

    public Member(MemberCreateRequestDto dto, String coupleCode, String socialId) {
        this.socialId = socialId;
        this.socialType = "KAKAO";
        this.nickname = dto.getNickname();
        this.birth = dto.getBirth();
        this.code = coupleCode;
        this.profileImage = DEFAULT_PROFILE_IMAGE;
        this.getRoles().add("USER");
    }

    public Member(MemberAppleCreateRequestDto dto, String coupleCode, String socialId) {
        this.socialId = socialId;
        this.socialType = "APPLE";
        this.nickname = dto.getNickname();
        this.birth = dto.getBirth();
        this.code = coupleCode;
        this.profileImage = DEFAULT_PROFILE_IMAGE;
        this.getRoles().add("USER");
    }

    public void addAuth(Auth auth) {
        this.auth = auth;
        auth.setMember(this);
    }

    public void addCouple(Couple couple) {
        this.couple = couple;
        couple.getMemberList().add(this);
    }

    public void deleteCouple(String code) {
        this.code = code;
        this.couple = null;
    }

    public void deleteMember() {
        this.socialId = null;
        this.socialType = null;
        this.nickname = null;
        this.profileImage = "";
    }
}

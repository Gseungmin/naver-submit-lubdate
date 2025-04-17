package com.example.naver.domain.entity.member;

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

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Couple extends BaseEntity {

    @Id
    @GeneratedValue(generator = "couple_seq_id")
    @GenericGenerator(name = "couple_seq_id", strategy = "com.example.naver.domain.generator.CoupleIDGenerator")
    @Column(name = "coupleId")
    private Long id;
    private LocalDate beginningDate;
    private LocalDate recoverDate;
    private Long firstId;
    private Long secondId;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "couple")
    private List<Member> memberList = new ArrayList<>();

    public Couple(Member inviter, Member invitee) {
        firstId = inviter.getId();
        secondId = invitee.getId();
        this.beginningDate = LocalDate.now();
        inviter.addCouple(this);
        invitee.addCouple(this);
    }

    public void disConnectCouple(Member member1, Member member2, String code1, String code2) {
        member1.deleteCouple(code1);
        member2.deleteCouple(code2);
        this.memberList.clear();
        this.recoverDate = LocalDate.now().plusDays(30);
    }

    public void reConnectCouple(Member member1, Member member2) {
        this.memberList.clear();
        member1.addCouple(this);
        member2.addCouple(this);
        this.recoverDate = null;
    }
}

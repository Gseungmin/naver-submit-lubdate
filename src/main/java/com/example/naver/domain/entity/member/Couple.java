package com.example.naver.domain.entity.member;

import com.example.naver.domain.entity.base.BaseEntity;
import com.example.naver.domain.generator.SnowflakeId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Couple extends BaseEntity {

    @Id
    @GeneratedValue @SnowflakeId
    @Column(name = "coupleId")
    private Long id;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "couple")
    private List<Member> memberList = new ArrayList<>();
}

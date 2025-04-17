package com.example.naver.domain.repository;

import com.example.naver.domain.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m " +
            "left join fetch m.auth " +
            "where m.id = :id")
    Optional<Member> findById(Long id);

    @Query("select m from Member m " +
            "left join fetch m.auth " +
            "left join fetch m.couple " +
            "where m.id = :id")
    Optional<Member> findByIdWithCouple(Long id);

    @Query("select m from Member m " +
            "left join fetch m.couple " +
            "where m.id in :ids")
    List<Member> findByIdsWithCouple(List<Long> ids);

    @Query("select m from Member m " +
            "left join fetch m.auth " +
            "left join fetch m.couple " +
            "where m.code = :code")
    Optional<Member> findByCodeWithCouple(String code);

    @Query("select m from Member m " +
            "left join fetch m.roles " +
            "where m.socialId = :socialId")
    Optional<Member> findBySocialId(String socialId);

    @Query("select m from Member m " +
            "left join fetch m.auth " +
            "left join fetch m.couple c " +
            "where c.id = :coupleId")
    List<Member> findByCoupleIdWithCouple(Long coupleId);

    @Modifying
    @Query("update Member m set m.couple = null where m.id = :id")
    void setCoupleNullById(Long id);
}

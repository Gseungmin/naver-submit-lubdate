package com.example.naver.domain.repository;

import com.example.naver.domain.entity.member.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoupleRepository extends JpaRepository<Couple, Long> {

    @Query("select c from Couple c " +
            "where c.id = :id")
    Optional<Couple> findById(Long id);
}

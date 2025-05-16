package com.example.naver.domain.repository;

import com.example.naver.domain.entity.member.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {

    @Query("SELECT a FROM Auth a WHERE a.phoneNumber = :phone")
    Optional<Auth> findByPhoneNumber(String phone);
}

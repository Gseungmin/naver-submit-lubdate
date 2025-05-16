package com.example.naver.domain.repository;

import com.example.naver.domain.entity.story.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    @Query("select s from Story s " +
            "where s.id = :id")
    Optional<Story> findByIdOnlyStory(Long id);

    @Query("select s from Story s " +
            "where s.coupleIdentifier = :coupleId AND " +
            "s.status = true " +
            "order by s.lastModifiedDate ASC")
    List<Story> findByAllByCoupleId(Long coupleId);

    @Modifying
    @Query("UPDATE Story s SET s.status = false, s.lastModifiedDate = CURRENT_TIMESTAMP WHERE s.id IN :ids")
    int bulkUpdateStatusToFalse(List<Long> ids);
}

package com.example.demo.repository;

import com.example.demo.entity.Notice;
import com.example.demo.entity.NoticeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByIsActiveTrueOrderByIsPinnedDescCreatedAtDesc();
    List<Notice> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(NoticeCategory category);
    List<Notice> findByTitleContainingAndIsActiveTrue(String keyword);
}
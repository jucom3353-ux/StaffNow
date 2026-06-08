package com.example.demo.repository;

import com.example.demo.entity.JobCategory;
import com.example.demo.entity.PreferredCategory;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PreferredCategoryRepository extends JpaRepository<PreferredCategory, Long> {

    List<PreferredCategory> findByUser(User user);

    boolean existsByUserAndCategory(User user, JobCategory category);

    void deleteByUserAndCategory(User user, JobCategory category);

    // 특정 카테고리를 선호하는 구직자 목록 (새 공고 팝업용)
    @Query("SELECT pc.user FROM PreferredCategory pc " +
           "WHERE pc.category = :category " +
           "AND pc.user.suspended = false")
    List<User> findWorkersByCategory(@Param("category") JobCategory category);

    // 구직자의 선호 카테고리 ID 목록 (부스트 팝업 매칭용)
    @Query("SELECT pc.category.id FROM PreferredCategory pc WHERE pc.user = :user")
    List<Long> findCategoryIdsByUser(@Param("user") User user);
}
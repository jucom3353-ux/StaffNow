package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileImageRepository extends JpaRepository<UserProfileImage, Long> {

    List<UserProfileImage> findByUserOrderByOrderIndexAsc(User user);

    int countByUser(User user);

    Optional<UserProfileImage> findByIdAndUser(Long id, User user);

    // 대표 사진 조회 (orderIndex = 0)
    Optional<UserProfileImage> findByUserAndOrderIndex(User user, int orderIndex);
}
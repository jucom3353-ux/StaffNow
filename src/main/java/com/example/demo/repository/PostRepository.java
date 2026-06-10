package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostCategory;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(
            PostCategory category, Pageable pageable);
    Page<Post> findByTitleContainingAndIsActiveTrue(String keyword, Pageable pageable);
    List<Post> findByUserAndIsActiveTrue(User user);
}
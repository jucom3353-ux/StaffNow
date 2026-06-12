package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Bookmark;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;


public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndJobPost(User user, JobPost jobPost);

    Optional<Bookmark> findByUserAndJobPost(User user, JobPost jobPost);

    List<Bookmark> findByUser(User user);
}
package com.example.demo.repository;

import com.example.demo.entity.Bookmark;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserAndJobPost(User user, JobPost jobPost);

    Optional<Bookmark> findByUserAndJobPost(User user, JobPost jobPost);

    List<Bookmark> findByUser(User user);
}
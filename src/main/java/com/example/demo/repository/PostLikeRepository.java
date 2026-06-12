package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;


public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, User user);
    Optional<PostLike> findByPostAndUser(Post post, User user);
}
package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, User user);
    Optional<PostLike> findByPostAndUser(Post post, User user);
}
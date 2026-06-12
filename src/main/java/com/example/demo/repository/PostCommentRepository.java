package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostComment;


public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostAndIsActiveTrueOrderByCreatedAtAsc(Post post);
}
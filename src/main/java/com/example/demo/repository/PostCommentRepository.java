package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostAndIsActiveTrueOrderByCreatedAtAsc(Post post);
}
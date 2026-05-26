package com.example.demo.service;

import com.example.demo.dto.PostCommentRequestDto;
import com.example.demo.dto.PostCommentResponseDto;
import com.example.demo.dto.PostRequestDto;
import com.example.demo.dto.PostResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.PostCommentRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostLikeRepository postLikeRepository;

    // 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPosts(
            PostCategory category, String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        if (keyword != null && !keyword.isBlank()) {
            return postRepository.findByTitleContainingAndIsActiveTrue(keyword, pageable)
                    .map(PostResponseDto::new);
        }
        if (category != null) {
            return postRepository.findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(
                    category, pageable).map(PostResponseDto::new);
        }
        return postRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(PostResponseDto::new);
    }

    // 게시글 단건 조회
    @Transactional
    public PostResponseDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        return new PostResponseDto(post);
    }

    // 게시글 작성
    @Transactional
    public PostResponseDto createPost(PostRequestDto requestDto, User loginUser) {
        Post post = new Post();
        post.setUser(loginUser);
        post.setCategory(requestDto.getCategory());
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        return new PostResponseDto(postRepository.save(post));
    }

    // 게시글 수정
    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto requestDto, User loginUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        return new PostResponseDto(postRepository.save(post));
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long id, User loginUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(loginUser.getId())
                && loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        post.setActive(false);
        postRepository.save(post);
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public List<PostCommentResponseDto> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        return postCommentRepository.findByPostAndIsActiveTrueOrderByCreatedAtAsc(post)
                .stream().map(PostCommentResponseDto::new).collect(Collectors.toList());
    }

    // 댓글 작성
    @Transactional
    public PostCommentResponseDto createComment(
            Long postId, PostCommentRequestDto requestDto, User loginUser) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(loginUser);
        comment.setContent(requestDto.getContent());

        return new PostCommentResponseDto(postCommentRepository.save(comment));
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, User loginUser) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(loginUser.getId())
                && loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        comment.setActive(false);
        postCommentRepository.save(comment);
    }

    // 좋아요 토글
    @Transactional
    public boolean toggleLike(Long postId, User loginUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (postLikeRepository.existsByPostAndUser(post, loginUser)) {
            PostLike like = postLikeRepository.findByPostAndUser(post, loginUser).get();
            postLikeRepository.delete(like);
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            return false;
        } else {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(loginUser);
            postLikeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
            return true;
        }
    }

    // 내 게시글 조회
    @Transactional(readOnly = true)
    public List<PostResponseDto> getMyPosts(User loginUser) {
        return postRepository.findByUserAndIsActiveTrue(loginUser)
                .stream().map(PostResponseDto::new).collect(Collectors.toList());
    }
}
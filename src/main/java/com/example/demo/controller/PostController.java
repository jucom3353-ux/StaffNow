package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PostCommentRequestDto;
import com.example.demo.dto.PostRequestDto;
import com.example.demo.entity.PostCategory;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
  
import org.springframework.web.bind.annotation.*;

@Tag(name = "게시판 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getPosts(
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                postService.getPosts(category, keyword, page, size)));
    }

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getPost(id)));
    }

    @Operation(summary = "게시글 작성")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPost(
            @RequestBody PostRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                postService.createPost(requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                postService.updatePost(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePost(@PathVariable Long id) {
        postService.deletePost(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("게시글 삭제 완료"));
    }

    @Operation(summary = "댓글 조회")
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<?>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(postService.getComments(id)));
    }

    @Operation(summary = "댓글 작성")
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<?>> createComment(
            @PathVariable Long id,
            @RequestBody PostCommentRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.ok(
                postService.createComment(id, requestDto,  AuthorizationUtil.getLoginUser())));
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<?>> deleteComment(
            @PathVariable Long commentId) {
        postService.deleteComment(commentId,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok("댓글 삭제 완료"));
    }

    @Operation(summary = "좋아요 토글")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<?>> toggleLike(@PathVariable Long id) {
        boolean liked = postService.toggleLike(id,  AuthorizationUtil.getLoginUser());
        return ResponseEntity.ok(ApiResponse.ok(liked ? "좋아요" : "좋아요 취소"));
    }

    @Operation(summary = "내 게시글 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyPosts() {
        return ResponseEntity.ok(ApiResponse.ok(postService.getMyPosts( AuthorizationUtil.getLoginUser())));
    }

     
}
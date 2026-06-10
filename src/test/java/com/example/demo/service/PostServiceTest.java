package com.example.demo.service;

import com.example.demo.dto.PostRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.PostCommentRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock private PostRepository postRepository;
    @Mock private PostCommentRepository postCommentRepository;
    @Mock private PostLikeRepository postLikeRepository;

    // 게시글 조회 - 없음
    @Test
    void getPost_notFound_throwsException() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    // 게시글 수정 - 본인 아님
    @Test
    void updatePost_notOwner_throwsException() {
        User owner = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        Post post = new Post();
        post.setUser(owner);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(1L, new PostRequestDto(), other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    // 게시글 삭제 - 본인도 아니고 어드민도 아님
    @Test
    void deletePost_notOwnerNotAdmin_throwsException() {
        User owner = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        Post post = new Post();
        post.setUser(owner);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.deletePost(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    // 댓글 조회 - 게시글 없음
    @Test
    void getComments_postNotFound_throwsException() {
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getComments(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    // 댓글 삭제 - 본인 아님
    @Test
    void deleteComment_notOwner_throwsException() {
        User owner = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        PostComment comment = new PostComment();
        comment.setUser(owner);

        given(postCommentRepository.findById(1L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> postService.deleteComment(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    // 좋아요 토글 - 게시글 없음
    @Test
    void toggleLike_postNotFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.toggleLike(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
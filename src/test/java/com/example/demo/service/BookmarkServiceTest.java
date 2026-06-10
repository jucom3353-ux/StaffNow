package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.JobPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @InjectMocks
    private BookmarkService bookmarkService;

    @Mock private BookmarkRepository bookmarkRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private ApplicationRepository applicationRepository;

    // 북마크 추가 - 공고 없음
    @Test
    void addBookmark_jobPostNotFound_throwsException() {
        User user = makeUser(1L);
        given(jobPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookmarkService.addBookmark(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공고를 찾을 수 없습니다");
    }

    // 북마크 추가 - 이미 북마크
    @Test
    void addBookmark_alreadyBookmarked_throwsException() {
        User user = makeUser(1L);
        JobPost post = makeJobPost(1L);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(bookmarkRepository.existsByUserAndJobPost(user, post)).willReturn(true);

        assertThatThrownBy(() -> bookmarkService.addBookmark(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 북마크한 공고");
    }

    // 북마크 추가 - 정상
    @Test
    void addBookmark_success() {
        User user = makeUser(1L);
        JobPost post = makeJobPost(1L);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(bookmarkRepository.existsByUserAndJobPost(user, post)).willReturn(false);

        bookmarkService.addBookmark(1L, user);

        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    // 북마크 삭제 - 공고 없음
    @Test
    void removeBookmark_jobPostNotFound_throwsException() {
        User user = makeUser(1L);
        given(jobPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookmarkService.removeBookmark(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공고를 찾을 수 없습니다");
    }

    // 북마크 삭제 - 북마크 없음
    @Test
    void removeBookmark_bookmarkNotFound_throwsException() {
        User user = makeUser(1L);
        JobPost post = makeJobPost(1L);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(bookmarkRepository.findByUserAndJobPost(user, post)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookmarkService.removeBookmark(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("북마크를 찾을 수 없습니다");
    }

    // 북마크 삭제 - 정상
    @Test
    void removeBookmark_success() {
        User user = makeUser(1L);
        JobPost post = makeJobPost(1L);
        Bookmark bookmark = new Bookmark();

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(bookmarkRepository.findByUserAndJobPost(user, post)).willReturn(Optional.of(bookmark));

        bookmarkService.removeBookmark(1L, user);

        verify(bookmarkRepository).delete(bookmark);
    }

    private User makeUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.INDIVIDUAL);
        return user;
    }

    private JobPost makeJobPost(Long id) {
        JobPost post = new JobPost();
        post.setId(id);
        return post;
    }
}
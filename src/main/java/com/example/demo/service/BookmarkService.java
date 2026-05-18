package com.example.demo.service;

import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    // 북마크 추가
    @Transactional
    public void addBookmark(Long jobPostId, User loginUser) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (bookmarkRepository.existsByUserAndJobPost(loginUser, jobPost)) {
            throw new RuntimeException("이미 북마크한 공고입니다.");
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(loginUser);
        bookmark.setJobPost(jobPost);
        bookmarkRepository.save(bookmark);
    }

    // 북마크 취소
    @Transactional
    public void removeBookmark(Long jobPostId, User loginUser) {

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        Bookmark bookmark = bookmarkRepository
                .findByUserAndJobPost(loginUser, jobPost)
                .orElseThrow(() -> new RuntimeException("북마크 없음"));

        bookmarkRepository.delete(bookmark);
    }

    // 내 북마크 목록 조회
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getMyBookmarks(User loginUser) {
        return bookmarkRepository.findByUser(loginUser).stream()
                .map(bookmark -> new JobPostResponseDto(
                        bookmark.getJobPost(),
                        applicationRepository.countByJobPost(bookmark.getJobPost())
                ))
                .collect(Collectors.toList());
    }
}
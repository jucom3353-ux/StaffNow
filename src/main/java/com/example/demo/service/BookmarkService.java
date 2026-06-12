package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.Bookmark;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public void addBookmark(Long jobPostId, User loginUser) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (bookmarkRepository.existsByUserAndJobPost(loginUser, jobPost)) {
            throw new CustomException(ErrorCode.ALREADY_BOOKMARKED);
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(loginUser);
        bookmark.setJobPost(jobPost);
        bookmarkRepository.save(bookmark);
    }

    @Transactional
    public void removeBookmark(Long jobPostId, User loginUser) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        Bookmark bookmark = bookmarkRepository.findByUserAndJobPost(loginUser, jobPost)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);
    }

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
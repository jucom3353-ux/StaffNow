package com.example.demo.service;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.JobPost;
import com.example.demo.repository.JobPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostService {

    private final JobPostRepository jobPostRepository;

    public JobPostService(JobPostRepository jobPostRepository) {
        this.jobPostRepository = jobPostRepository;
    }

    // 공고 등록
    @Transactional
    public void createJobPost(JobPostCreateRequestDto requestDto) {

        JobPost jobPost = new JobPost();

        jobPost.setTitle(requestDto.getTitle());
        jobPost.setContent(requestDto.getContent());

        jobPostRepository.save(jobPost);
    }

    // 공고 전체 조회
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getJobPosts() {

        return jobPostRepository.findAll().stream()
                .map(post -> new JobPostResponseDto(
                        post.getTitle(),
                        post.getContent()
                ))
                .collect(Collectors.toList());
    }

    // 공고 단건 조회
    @Transactional(readOnly = true)
    public JobPostResponseDto getJobPost(Long id) {

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        return new JobPostResponseDto(
                post.getTitle(),
                post.getContent()
        );
    }

    // 공고 수정
    @Transactional
    public void updateJobPost(
            Long id,
            JobPostCreateRequestDto requestDto
    ) {

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        jobPostRepository.save(post);
    }

    // 공고 삭제
    @Transactional
    public void deleteJobPost(Long id) {

        jobPostRepository.deleteById(id);
    }
}
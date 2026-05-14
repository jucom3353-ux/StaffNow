package com.example.demo.service;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostPageResponseDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    public JobPostService(
            JobPostRepository jobPostRepository,
            ApplicationRepository applicationRepository
    ) {
        this.jobPostRepository = jobPostRepository;
        this.applicationRepository = applicationRepository;
    }

    // 공고 등록
    @Transactional
    public void createJobPost(JobPostCreateRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 공고를 등록할 수 있습니다");
        }

        JobPost jobPost = new JobPost();
        jobPost.setTitle(requestDto.getTitle());
        jobPost.setContent(requestDto.getContent());
        jobPost.setWorkLocation(requestDto.getWorkLocation());
        jobPost.setStartTime(requestDto.getStartTime());
        jobPost.setEndTime(requestDto.getEndTime());
        jobPost.setBreakTime(requestDto.getBreakTime());
        jobPost.setWageType(requestDto.getWageType());
        jobPost.setWageAmount(requestDto.getWageAmount());
        jobPost.setIncludeHolidayPay(requestDto.getIncludeHolidayPay());
        jobPost.setWorkType(requestDto.getWorkType());
        jobPost.setDescription(requestDto.getDescription());
        jobPost.setRequiredGender(requestDto.getRequiredGender());
        jobPost.setRequiredAgeMin(requestDto.getRequiredAgeMin());
        jobPost.setRequiredAgeMax(requestDto.getRequiredAgeMax());
        jobPost.setRequiredPersonality(requestDto.getRequiredPersonality());
        jobPost.setRequiredCondition(requestDto.getRequiredCondition());
        jobPost.setPreferredExperience(requestDto.getPreferredExperience());
        jobPost.setPreferredLanguage(requestDto.getPreferredLanguage());
        jobPost.setPreferredEtc(requestDto.getPreferredEtc());
        jobPost.setRecruitCount(requestDto.getRecruitCount());
        jobPost.setPostStatus(
                requestDto.getPostStatus() != null
                        ? requestDto.getPostStatus()
                        : PostStatus.DRAFT
        );
        jobPost.setCategory(requestDto.getCategory());
        jobPost.setDeadline(requestDto.getDeadline());
        jobPost.setViewCount(0);
        jobPost.setUser(loginUser);
        jobPostRepository.save(jobPost);
    }

    // 구직자용 공고 검색/필터/정렬 (페이지네이션)
    @Transactional
    public JobPostPageResponseDto searchJobPosts(
            String title,
            String workLocation,
            String companyName,
            JobCategory category,
            String sort,
            int page,
            int size
    ) {
        // 정렬 기준
        Sort sorting = switch (sort != null ? sort : "latest") {
            case "wage" -> Sort.by(Sort.Direction.DESC, "wageAmount");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadline");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<JobPost> result = jobPostRepository.searchJobPostsWithPage(
                title, workLocation, PostStatus.OPEN, category, companyName, pageable
        );

        List<JobPostResponseDto> posts = result.getContent().stream()
                .map(post -> new JobPostResponseDto(
                        post,
                        applicationRepository.countByJobPost(post)
                ))
                .collect(Collectors.toList());

        return new JobPostPageResponseDto(
                posts,
                result.getNumber(),
                result.getTotalPages(),
                result.getTotalElements(),
                result.getSize()
        );
    }

    // 기업용 전체 공고 조회
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getJobPosts(
            String title,
            String workLocation,
            PostStatus postStatus
    ) {
        return jobPostRepository
                .searchJobPosts(title, workLocation, postStatus, null, null)
                .stream()
                .map(post -> new JobPostResponseDto(
                        post,
                        applicationRepository.countByJobPost(post)
                ))
                .collect(Collectors.toList());
    }

    // 단건 공고 조회 + 조회수 증가
    @Transactional
    public JobPostResponseDto getJobPost(Long id) {
        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        jobPostRepository.save(post);

        return new JobPostResponseDto(
                post,
                applicationRepository.countByJobPost(post)
        );
    }

    // 내 공고 조회 (기업용)
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getMyJobPosts(User loginUser, PostStatus postStatus) {
        return jobPostRepository.findAll().stream()
                .filter(post -> post.getUser().getId().equals(loginUser.getId()))
                .filter(post -> postStatus == null || post.getPostStatus() == postStatus)
                .map(post -> new JobPostResponseDto(
                        post,
                        applicationRepository.countByJobPost(post)
                ))
                .collect(Collectors.toList());
    }

    // 공고 상태 변경
    @Transactional
    public void changePostStatus(Long id, PostStatus postStatus, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 공고 상태를 변경할 수 있습니다");
        }

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!post.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 상태 변경 가능");
        }

        post.setPostStatus(postStatus);
        jobPostRepository.save(post);
    }

    // 공고 수정
    @Transactional
    public void updateJobPost(Long id, JobPostCreateRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 공고를 수정할 수 있습니다");
        }

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!post.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 수정 가능");
        }

        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());
        post.setWorkLocation(requestDto.getWorkLocation());
        post.setStartTime(requestDto.getStartTime());
        post.setEndTime(requestDto.getEndTime());
        post.setBreakTime(requestDto.getBreakTime());
        post.setWageType(requestDto.getWageType());
        post.setWageAmount(requestDto.getWageAmount());
        post.setIncludeHolidayPay(requestDto.getIncludeHolidayPay());
        post.setWorkType(requestDto.getWorkType());
        post.setDescription(requestDto.getDescription());
        post.setRequiredGender(requestDto.getRequiredGender());
        post.setRequiredAgeMin(requestDto.getRequiredAgeMin());
        post.setRequiredAgeMax(requestDto.getRequiredAgeMax());
        post.setRequiredPersonality(requestDto.getRequiredPersonality());
        post.setRequiredCondition(requestDto.getRequiredCondition());
        post.setPreferredExperience(requestDto.getPreferredExperience());
        post.setPreferredLanguage(requestDto.getPreferredLanguage());
        post.setPreferredEtc(requestDto.getPreferredEtc());
        post.setRecruitCount(requestDto.getRecruitCount());
        post.setCategory(requestDto.getCategory());
        post.setDeadline(requestDto.getDeadline());
        if (requestDto.getPostStatus() != null) {
            post.setPostStatus(requestDto.getPostStatus());
        }

        jobPostRepository.save(post);
    }

    // 공고 삭제
    @Transactional
    public void deleteJobPost(Long id, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 공고를 삭제할 수 있습니다");
        }

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!post.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 삭제 가능");
        }

        jobPostRepository.deleteById(id);
    }
}
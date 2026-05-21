package com.example.demo.service;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostPageResponseDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.JobPostViewHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobPostViewHistoryRepository jobPostViewHistoryRepository;

    public JobPostService(
            JobPostRepository jobPostRepository,
            ApplicationRepository applicationRepository,
            JobCategoryRepository jobCategoryRepository,
            JobPostViewHistoryRepository jobPostViewHistoryRepository
    ) {
        this.jobPostRepository = jobPostRepository;
        this.applicationRepository = applicationRepository;
        this.jobCategoryRepository = jobCategoryRepository;
        this.jobPostViewHistoryRepository = jobPostViewHistoryRepository;
    }

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
        jobPost.setDeadline(requestDto.getDeadline());
        jobPost.setViewCount(0);
        jobPost.setUser(loginUser);
        jobPost.setWorkStartDate(requestDto.getWorkStartDate());
        jobPost.setWorkEndDate(requestDto.getWorkEndDate());
        jobPost.setMealProvided(requestDto.getMealProvided() != null
                ? requestDto.getMealProvided() : false);
        jobPost.setUniformInfo(requestDto.getUniformInfo());
        jobPost.setManagerName(requestDto.getManagerName());
        jobPost.setManagerPhone(requestDto.getManagerPhone());
        jobPost.setManagerEmail(requestDto.getManagerEmail());
        jobPost.setManagerFax(requestDto.getManagerFax());

        if (requestDto.getCategoryId() != null) {
            JobCategory category = jobCategoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리 없음"));
            jobPost.setCategory(category);
        }

        jobPostRepository.save(jobPost);
    }

    @Transactional
    public JobPostPageResponseDto searchJobPosts(
            String title,
            String workLocation,
            String companyName,
            Long categoryId,
            String sort,
            int page,
            int size
    ) {
        Sort sorting = switch (sort != null ? sort : "latest") {
            case "wage" -> Sort.by(Sort.Direction.DESC, "wageAmount");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadline");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<JobPost> result = jobPostRepository.searchJobPostsWithPage(
                title, workLocation, PostStatus.OPEN, categoryId, companyName, pageable
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

    // 단건 조회 + 조회수 증가 + 최근 본 공고 저장
    @Transactional
    public JobPostResponseDto getJobPost(Long id, User loginUser) {
        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        post.setViewCount(
                (post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        jobPostRepository.save(post);

        // 구직자만 최근 본 공고 저장
        if (loginUser != null && loginUser.getRole() == Role.INDIVIDUAL) {
            jobPostViewHistoryRepository
                    .findByUserAndJobPost(loginUser, post)
                    .ifPresentOrElse(
                            history -> {
                                // 이미 있으면 viewedAt 갱신
                                history.setViewedAt(LocalDateTime.now());
                                jobPostViewHistoryRepository.save(history);
                            },
                            () -> {
                                // 없으면 새로 생성
                                JobPostViewHistory history = new JobPostViewHistory();
                                history.setUser(loginUser);
                                history.setJobPost(post);
                                jobPostViewHistoryRepository.save(history);
                            }
                    );
        }

        return new JobPostResponseDto(
                post,
                applicationRepository.countByJobPost(post)
        );
    }

    // 최근 본 공고 목록 (구직자용)
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getRecentViews(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("구직자만 조회 가능합니다.");
        }

        return jobPostViewHistoryRepository
                .findByUserOrderByViewedAtDesc(loginUser)
                .stream()
                .limit(20)
                .map(h -> new JobPostResponseDto(
                        h.getJobPost(),
                        applicationRepository.countByJobPost(h.getJobPost())
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getMyJobPosts(User loginUser, PostStatus postStatus) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        List<JobPost> posts = jobPostRepository.findByUser(loginUser);

        return posts.stream()
                .filter(post -> postStatus == null ||
                        post.getPostStatus() == postStatus)
                .map(post -> new JobPostResponseDto(
                        post,
                        applicationRepository.countByJobPost(post)
                ))
                .collect(Collectors.toList());
    }

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
        post.setDeadline(requestDto.getDeadline());
        if (requestDto.getPostStatus() != null) {
            post.setPostStatus(requestDto.getPostStatus());
        }
        post.setWorkStartDate(requestDto.getWorkStartDate());
        post.setWorkEndDate(requestDto.getWorkEndDate());
        post.setMealProvided(requestDto.getMealProvided());
        post.setUniformInfo(requestDto.getUniformInfo());
        post.setManagerName(requestDto.getManagerName());
        post.setManagerPhone(requestDto.getManagerPhone());
        post.setManagerEmail(requestDto.getManagerEmail());
        post.setManagerFax(requestDto.getManagerFax());

        if (requestDto.getCategoryId() != null) {
            JobCategory category = jobCategoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리 없음"));
            post.setCategory(category);
        }

        jobPostRepository.save(post);
    }

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

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getPopularJobPosts(int limit, String region) {
        Pageable pageable = PageRequest.of(0, limit);

        List<JobPost> posts = (region != null && !region.isBlank())
                ? jobPostRepository.findPopularJobPostsByRegion(region, pageable)
                : jobPostRepository.findPopularJobPosts(pageable);

        return posts.stream()
                .map(post -> new JobPostResponseDto(
                        post,
                        applicationRepository.countByJobPost(post)
                ))
                .collect(Collectors.toList());
    }

    // ===== ADMIN 전용 =====

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> adminGetAllJobPosts(
            PostStatus postStatus, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 조회 가능합니다.");
        }

        List<JobPost> posts = postStatus != null
                ? jobPostRepository.findByPostStatus(postStatus)
                : jobPostRepository.findAll();

        return posts.stream()
                .map(post -> new JobPostResponseDto(
                        post,
                        applicationRepository.countByJobPost(post)
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void adminCloseJobPost(Long id, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 강제 마감 가능합니다.");
        }

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        post.setPostStatus(PostStatus.CLOSED);
        jobPostRepository.save(post);
    }

    @Transactional
    public void adminDeleteJobPost(Long id, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 강제 삭제 가능합니다.");
        }

        jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        jobPostRepository.deleteById(id);
    }
}
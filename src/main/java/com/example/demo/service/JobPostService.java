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

import java.time.LocalDate;
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

        // ✅ 6번: 마감일 유효성 검증
        validateDeadline(requestDto.getDeadline());

        JobPost jobPost = new JobPost();
        applyJobPostFields(jobPost, requestDto);
        jobPost.setPostStatus(
                requestDto.getPostStatus() != null
                        ? requestDto.getPostStatus()
                        : PostStatus.DRAFT
        );
        jobPost.setViewCount(0);
        jobPost.setUser(loginUser);
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

    // ✅ 3번: 조회수 동시성 해결
    @Transactional
    public JobPostResponseDto getJobPost(Long id, User loginUser) {
        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        jobPostRepository.incrementViewCount(id);

        if (loginUser != null && loginUser.getRole() == Role.INDIVIDUAL) {
            jobPostViewHistoryRepository
                    .findByUserAndJobPost(loginUser, post)
                    .ifPresentOrElse(
                            history -> {
                                history.setViewedAt(LocalDateTime.now());
                                jobPostViewHistoryRepository.save(history);
                            },
                            () -> {
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

    // ✅ 4번: getMyJobPosts DB 필터링
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getMyJobPosts(User loginUser, PostStatus postStatus) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        List<JobPost> posts = postStatus != null
                ? jobPostRepository.findByUserAndPostStatus(loginUser, postStatus)
                : jobPostRepository.findByUser(loginUser);

        return posts.stream()
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

        // ✅ 6번: 마감일 유효성 검증
        validateDeadline(requestDto.getDeadline());

        // ✅ 5번: 코드 중복 제거
        applyJobPostFields(post, requestDto);

        if (requestDto.getPostStatus() != null) {
            post.setPostStatus(requestDto.getPostStatus());
        }

        jobPostRepository.save(post);
    }

    // ✅ 2번: 공고 복사
    @Transactional
    public void copyJobPost(Long id, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 공고를 복사할 수 있습니다");
        }

        JobPost original = jobPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!original.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 복사 가능");
        }

        JobPost copy = new JobPost();
        copy.setTitle("[복사] " + original.getTitle());
        copy.setContent(original.getContent());
        copy.setWorkLocation(original.getWorkLocation());
        copy.setStartTime(original.getStartTime());
        copy.setEndTime(original.getEndTime());
        copy.setBreakTime(original.getBreakTime());
        copy.setWageType(original.getWageType());
        copy.setWageAmount(original.getWageAmount());
        copy.setIncludeHolidayPay(original.getIncludeHolidayPay());
        copy.setWorkType(original.getWorkType());
        copy.setDescription(original.getDescription());
        copy.setRequiredGender(original.getRequiredGender());
        copy.setRequiredAgeMin(original.getRequiredAgeMin());
        copy.setRequiredAgeMax(original.getRequiredAgeMax());
        copy.setRequiredPersonality(original.getRequiredPersonality());
        copy.setRequiredCondition(original.getRequiredCondition());
        copy.setPreferredExperience(original.getPreferredExperience());
        copy.setPreferredLanguage(original.getPreferredLanguage());
        copy.setPreferredEtc(original.getPreferredEtc());
        copy.setRecruitCount(original.getRecruitCount());
        copy.setCategory(original.getCategory());
        copy.setDeadline(null); // 마감일은 새로 설정
        copy.setWorkStartDate(null); // 근무일도 새로 설정
        copy.setWorkEndDate(null);
        copy.setMealProvided(original.getMealProvided());
        copy.setUniformInfo(original.getUniformInfo());
        copy.setManagerName(original.getManagerName());
        copy.setManagerPhone(original.getManagerPhone());
        copy.setManagerEmail(original.getManagerEmail());
        copy.setManagerFax(original.getManagerFax());
        copy.setPostStatus(PostStatus.DRAFT); // 복사본은 항상 DRAFT
        copy.setViewCount(0);
        copy.setUser(loginUser);

        jobPostRepository.save(copy);
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

    // ✅ 5번: create/update 공통 필드 추출
    private void applyJobPostFields(JobPost post, JobPostCreateRequestDto dto) {
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setWorkLocation(dto.getWorkLocation());
        post.setStartTime(dto.getStartTime());
        post.setEndTime(dto.getEndTime());
        post.setBreakTime(dto.getBreakTime());
        post.setWageType(dto.getWageType());
        post.setWageAmount(dto.getWageAmount());
        post.setIncludeHolidayPay(dto.getIncludeHolidayPay());
        post.setWorkType(dto.getWorkType());
        post.setDescription(dto.getDescription());
        post.setRequiredGender(dto.getRequiredGender());
        post.setRequiredAgeMin(dto.getRequiredAgeMin());
        post.setRequiredAgeMax(dto.getRequiredAgeMax());
        post.setRequiredPersonality(dto.getRequiredPersonality());
        post.setRequiredCondition(dto.getRequiredCondition());
        post.setPreferredExperience(dto.getPreferredExperience());
        post.setPreferredLanguage(dto.getPreferredLanguage());
        post.setPreferredEtc(dto.getPreferredEtc());
        post.setRecruitCount(dto.getRecruitCount());
        post.setDeadline(dto.getDeadline());
        post.setWorkStartDate(dto.getWorkStartDate());
        post.setWorkEndDate(dto.getWorkEndDate());
        post.setMealProvided(dto.getMealProvided() != null ? dto.getMealProvided() : false);
        post.setUniformInfo(dto.getUniformInfo());
        post.setManagerName(dto.getManagerName());
        post.setManagerPhone(dto.getManagerPhone());
        post.setManagerEmail(dto.getManagerEmail());
        post.setManagerFax(dto.getManagerFax());

        if (dto.getCategoryId() != null) {
            JobCategory category = jobCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("카테고리 없음"));
            post.setCategory(category);
        }
    }

    // ✅ 6번: 마감일 유효성 검증
    private void validateDeadline(String deadline) {
        if (deadline == null || deadline.isBlank()) return;
        try {
            LocalDate deadlineDate = LocalDate.parse(deadline);
            if (deadlineDate.isBefore(LocalDate.now())) {
                throw new RuntimeException("마감일은 오늘 이후 날짜여야 합니다.");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("마감일 형식이 올바르지 않습니다. (yyyy-MM-dd)");
        }
    }
}
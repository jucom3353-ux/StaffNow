package com.example.demo.service;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostPageResponseDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.JobPostViewHistoryRepository;
import com.example.demo.repository.PreferredCategoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.AuthorizationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobPostViewHistoryRepository jobPostViewHistoryRepository;
    private final PreferredCategoryRepository preferredCategoryRepository;
    private final SubscriptionService subscriptionService;
    private final KakaoGeocodingService kakaoGeocodingService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private void validateJobPostOwnership(JobPost post, User loginUser) {
        if (!AuthorizationUtil.isMyJobPost(post, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }
    }

    @Transactional
    public void createJobPost(JobPostCreateRequestDto requestDto, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        if (!subscriptionService.canPostJob(companyUser)) {
            throw new CustomException(ErrorCode.JOB_POST_LIMIT_EXCEEDED);
        }

        validateDeadline(requestDto.getDeadline());

        JobPost jobPost = new JobPost();
        applyJobPostFields(jobPost, requestDto);
        jobPost.setPostStatus(requestDto.getPostStatus() != null
                ? requestDto.getPostStatus() : PostStatus.DRAFT);
        jobPost.setViewCount(0);
        jobPost.setUser(loginUser);
        JobPost saved = jobPostRepository.save(jobPost);

        if (saved.getPostStatus() == PostStatus.OPEN) {
            // 새 공고 팝업: 해당 카테고리 선호 구직자에게 발송
            if (saved.getCategory() != null) {
                sendNewJobPostPopup(saved);
            }
            // 긴급 공고 알림
            if (Boolean.TRUE.equals(saved.getUrgentBadge())) {
                sendUrgentNotification(saved);
            }
        }
    }

    @Transactional
    public JobPostPageResponseDto searchJobPosts(String title, String workLocation,
            String companyName, Long categoryId, String sort, int page, int size) {

        Sort sorting = switch (sort != null ? sort : "latest") {
            case "wage" -> Sort.by(Sort.Direction.DESC, "wageAmount");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "deadline");
            case "popular" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(page, size, sorting);
        Page<JobPost> result = jobPostRepository.searchJobPostsWithPage(
                title, workLocation, PostStatus.OPEN, categoryId, companyName, pageable);

        List<JobPostResponseDto> posts = result.getContent().stream()
                .map(post -> new JobPostResponseDto(post,
                        applicationRepository.countByJobPost(post)))
                .collect(Collectors.toList());

        return new JobPostPageResponseDto(posts, result.getNumber(),
                result.getTotalPages(), result.getTotalElements(), result.getSize());
    }

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getJobPosts(String title, String workLocation,
                                                 PostStatus postStatus) {
        return jobPostRepository.searchJobPosts(title, workLocation, postStatus, null, null)
                .stream()
                .map(post -> new JobPostResponseDto(post,
                        applicationRepository.countByJobPost(post)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobPostPageResponseDto getUrgentJobPosts(int page, int size) {
        String today = LocalDate.now().toString();
        String threeDaysLater = LocalDate.now().plusDays(3).toString();

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "deadline"));

        Page<JobPost> result = jobPostRepository.findUrgentJobPostsWithPage(
                today, threeDaysLater, pageable);

        List<JobPostResponseDto> posts = result.getContent().stream()
                .map(post -> new JobPostResponseDto(post,
                        applicationRepository.countByJobPost(post)))
                .collect(Collectors.toList());

        return new JobPostPageResponseDto(posts, result.getNumber(),
                result.getTotalPages(), result.getTotalElements(), result.getSize());
    }

    @Transactional
    public JobPostResponseDto getJobPost(Long id, User loginUser) {
        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        jobPostRepository.incrementViewCount(id);

        if (loginUser != null && loginUser.getRole() == Role.INDIVIDUAL) {
            jobPostViewHistoryRepository.findByUserAndJobPost(loginUser, post)
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

        return new JobPostResponseDto(post, applicationRepository.countByJobPost(post));
    }

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getRecentViews(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        return jobPostViewHistoryRepository.findByUserOrderByViewedAtDesc(loginUser)
                .stream().limit(20)
                .map(h -> new JobPostResponseDto(h.getJobPost(),
                        applicationRepository.countByJobPost(h.getJobPost())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getMyJobPosts(User loginUser, PostStatus postStatus) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        List<JobPost> posts = postStatus != null
                ? jobPostRepository.findByUserAndPostStatus(companyUser, postStatus)
                : jobPostRepository.findByUser(companyUser);

        return posts.stream()
                .map(post -> new JobPostResponseDto(post,
                        applicationRepository.countByJobPost(post)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void changePostStatus(Long id, PostStatus postStatus, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        validateJobPostOwnership(post, loginUser);
        post.setPostStatus(postStatus);
        jobPostRepository.save(post);

        if (postStatus == PostStatus.OPEN) {
            if (post.getCategory() != null) {
                sendNewJobPostPopup(post);
            }
            if (Boolean.TRUE.equals(post.getUrgentBadge())) {
                sendUrgentNotification(post);
            }
        }
    }

    @Transactional
    public void updateJobPost(Long id, JobPostCreateRequestDto requestDto, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        validateJobPostOwnership(post, loginUser);
        validateDeadline(requestDto.getDeadline());
        applyJobPostFields(post, requestDto);

        if (requestDto.getPostStatus() != null) {
            post.setPostStatus(requestDto.getPostStatus());
        }

        jobPostRepository.save(post);
    }

    @Transactional
    public void copyJobPost(Long id, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost original = jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        validateJobPostOwnership(original, loginUser);

        JobPost copy = new JobPost();
        copy.setTitle("[복사] " + original.getTitle());
        copy.setContent(original.getContent());
        copy.setWorkLocation(original.getWorkLocation());
        copy.setLatitude(original.getLatitude());
        copy.setLongitude(original.getLongitude());
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
        copy.setDeadline(null);
        copy.setWorkStartDate(null);
        copy.setWorkEndDate(null);
        copy.setMealProvided(original.getMealProvided());
        copy.setUniformInfo(original.getUniformInfo());
        copy.setManagerName(original.getManagerName());
        copy.setManagerPhone(original.getManagerPhone());
        copy.setManagerEmail(original.getManagerEmail());
        copy.setManagerFax(original.getManagerFax());
        copy.setImageUrl(original.getImageUrl());
        copy.setPostStatus(PostStatus.DRAFT);
        copy.setViewCount(0);
        copy.setUser(loginUser);
        jobPostRepository.save(copy);
    }

    @Transactional
    public void deleteJobPost(Long id, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        validateJobPostOwnership(post, loginUser);
        jobPostRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public JobPost getJobPostEntity(Long id, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));
        validateJobPostOwnership(post, loginUser);
        return post;
    }

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getPopularJobPosts(int limit, String region) {
        Pageable pageable = PageRequest.of(0, limit);
        List<JobPost> posts = (region != null && !region.isBlank())
                ? jobPostRepository.findPopularJobPostsByRegion(region, pageable)
                : jobPostRepository.findPopularJobPosts(pageable);

        return posts.stream()
                .map(post -> new JobPostResponseDto(post,
                        applicationRepository.countByJobPost(post)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> adminGetAllJobPosts(PostStatus postStatus, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<JobPost> posts = postStatus != null
                ? jobPostRepository.findByPostStatus(postStatus)
                : jobPostRepository.findAll();

        return posts.stream()
                .map(post -> new JobPostResponseDto(post,
                        applicationRepository.countByJobPost(post)))
                .collect(Collectors.toList());
    }

    @Transactional
    public void adminCloseJobPost(Long id, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        JobPost post = jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        post.setPostStatus(PostStatus.CLOSED);
        jobPostRepository.save(post);
    }

    @Transactional
    public void adminDeleteJobPost(Long id, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        jobPostRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        jobPostRepository.deleteById(id);
    }

    // 새 공고 팝업: 해당 카테고리 선호 구직자에게 WebSocket 팝업 발송
    private void sendNewJobPostPopup(JobPost jobPost) {
        try {
            List<User> workers = preferredCategoryRepository
                    .findWorkersByCategory(jobPost.getCategory());
            workers.forEach(worker ->
                    notificationService.sendPopup(
                            worker,
                            NotificationType.NEW_JOB_POST,
                            "[새 공고] " + jobPost.getTitle() + " - 지금 확인해보세요!",
                            jobPost.getId()
                    )
            );
        } catch (Exception e) {
            // 팝업 발송 실패해도 공고 등록은 정상 처리
        }
    }

    private void sendUrgentNotification(JobPost jobPost) {
        List<User> immediateWorkers = userRepository.findByRoleAndWorkAvailability(
                Role.INDIVIDUAL, WorkAvailability.IMMEDIATE);

        immediateWorkers.forEach(worker ->
                notificationService.send(
                        worker,
                        NotificationType.URGENT_JOB_POST,
                        "[긴급구인] " + jobPost.getTitle() + " - 즉시 지원 가능합니다.",
                        jobPost.getId()
                )
        );
    }

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
        post.setTopExposure(dto.getTopExposure() != null ? dto.getTopExposure() : false);
        post.setUrgentBadge(dto.getUrgentBadge() != null ? dto.getUrgentBadge() : false);

        if (dto.getCategoryId() != null) {
            JobCategory category = jobCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            post.setCategory(category);
        }

        post.setAllowOnline(dto.getAllowOnline() != null ? dto.getAllowOnline() : true);
        post.setAllowPhone(dto.getAllowPhone() != null ? dto.getAllowPhone() : false);
        post.setAllowSms(dto.getAllowSms() != null ? dto.getAllowSms() : false);

        if (dto.getWorkLocation() != null && !dto.getWorkLocation().isBlank()) {
            double[] coords = kakaoGeocodingService.getCoordinates(dto.getWorkLocation());
            if (coords != null) {
                post.setLatitude(coords[0]);
                post.setLongitude(coords[1]);
            }
        }

        if (dto.getImageUrl() != null) post.setImageUrl(dto.getImageUrl());
    }

    private void validateDeadline(String deadline) {
        if (deadline == null || deadline.isBlank()) return;
        try {
            LocalDate deadlineDate = LocalDate.parse(deadline);
            if (deadlineDate.isBefore(LocalDate.now())) {
                throw new CustomException(ErrorCode.DEADLINE_INVALID);
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DEADLINE_FORMAT_INVALID);
        }
    }
}
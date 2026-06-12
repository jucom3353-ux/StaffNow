package com.example.demo.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.HomeSummaryResponseDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.Payroll;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.PreferredWorkTime;
import com.example.demo.entity.Role;
import com.example.demo.entity.Skill;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkSession;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.JobPostViewHistoryRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PayrollRepository;
import com.example.demo.repository.PreferredWorkTimeRepository;
import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.WorkSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NotificationRepository notificationRepository;
    private final JobPostRepository jobPostRepository;
    private final WorkSessionRepository workSessionRepository;
    private final PayrollRepository payrollRepository;
    private final PreferredWorkTimeRepository preferredWorkTimeRepository;
    private final SkillRepository skillRepository;
    private final JobPostViewHistoryRepository jobPostViewHistoryRepository;

    @Transactional(readOnly = true)
    public HomeSummaryResponseDto getSummary(User loginUser) {
        if (loginUser.getRole() == Role.INDIVIDUAL) {
            return getWorkerSummary(loginUser);
        } else {
            return getCompanySummary(loginUser);
        }
    }

    // 추천 공고 목록 (별도 API용)
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getRecommendedJobPosts(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("구직자만 조회 가능합니다.");
        }
        return getRecommendedJobPostsInternal(loginUser)
                .stream()
                .map(post -> new JobPostResponseDto(post,
                        applicationRepository.countByJobPost(post)))
                .collect(Collectors.toList());
    }

    // 최근 본 공고 목록 (별도 API용)
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> getRecentViewedJobPosts(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("구직자만 조회 가능합니다.");
        }
        return jobPostViewHistoryRepository
                .findByUserOrderByViewedAtDesc(loginUser)
                .stream()
                .limit(20)
                .map(h -> new JobPostResponseDto(
                        h.getJobPost(),
                        applicationRepository.countByJobPost(h.getJobPost())))
                .collect(Collectors.toList());
    }

    // 근로자용 홈 요약
    private HomeSummaryResponseDto getWorkerSummary(User loginUser) {

        int appliedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.APPLIED);
        int approvedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.APPROVED);
        int rejectedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.REJECTED);

        int bookmarkCount = bookmarkRepository.findByUser(loginUser).size();

        String today = LocalDate.now().toString();
        int todayWorkCount = (int) applicationRepository
                .findByUser(loginUser, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPROVED)
                .filter(a -> a.getWorkSession() != null)
                .filter(a -> today.equals(a.getWorkSession().getWorkDate()))
                .count();

        int unreadCount = notificationRepository
                .countByUserAndIsReadFalse(loginUser);

        List<JobPostResponseDto> recommendedJobPosts =
                getRecommendedJobPostsInternal(loginUser)
                        .stream()
                        .map(post -> new JobPostResponseDto(post, 0))
                        .collect(Collectors.toList());

        // 최근 본 공고 5개 (홈 요약용)
        List<JobPostResponseDto> recentViewedJobPosts =
                jobPostViewHistoryRepository
                        .findByUserOrderByViewedAtDesc(loginUser)
                        .stream()
                        .limit(5)
                        .map(h -> new JobPostResponseDto(h.getJobPost(), 0))
                        .collect(Collectors.toList());

        return HomeSummaryResponseDto.builder()
                .unreadNotificationCount(unreadCount)
                .appliedCount(appliedCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .bookmarkCount(bookmarkCount)
                .todayWorkCount(todayWorkCount)
                .recommendedJobPosts(recommendedJobPosts)
                .recentViewedJobPosts(recentViewedJobPosts)
                .build();
    }

    // 기업용 홈 요약
    private HomeSummaryResponseDto getCompanySummary(User loginUser) {

        int openJobPostCount = jobPostRepository.findByUser(loginUser)
                .stream()
                .filter(p -> p.getPostStatus() == PostStatus.OPEN)
                .mapToInt(p -> 1)
                .sum();

        String today = LocalDate.now().toString();
        int todayShiftWorkerCount = jobPostRepository.findByUser(loginUser)
                .stream()
                .flatMap(jp -> workSessionRepository
                        .findByJobPostAndWorkDate(jp, today).stream())
                .mapToInt(WorkSession::getCurrentCount)
                .sum();

        int pendingApplicantCount = applicationRepository.countByCompanyAndStatus(
                loginUser, ApplicationStatus.APPLIED);

        String weekStart = LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY).toString();
        int thisWeekTotalPay = jobPostRepository.findByUser(loginUser)
                .stream()
                .flatMap(jp -> payrollRepository.findByJobPost(jp).stream())
                .filter(p -> p.getWorkWeekStart() != null &&
                             p.getWorkWeekStart().compareTo(weekStart) >= 0)
                .mapToInt(Payroll::getTotalPay)
                .sum();

        int unreadCount = notificationRepository
                .countByUserAndIsReadFalse(loginUser);

        return HomeSummaryResponseDto.builder()
                .unreadNotificationCount(unreadCount)
                .openJobPostCount(openJobPostCount)
                .todayShiftWorkerCount(todayShiftWorkerCount)
                .pendingApplicantCount(pendingApplicantCount)
                .thisWeekTotalPay(thisWeekTotalPay)
                .build();
    }

    // 추천 공고 알고리즘 내부용
    private List<JobPost> getRecommendedJobPostsInternal(User loginUser) {

        Set<Long> addedIds = new LinkedHashSet<>();
        List<JobPost> result = new ArrayList<>();

        // 1단계: 스킬 카테고리 매칭
        List<Skill> skills = skillRepository.findByUser(loginUser);
        if (!skills.isEmpty()) {
            for (Skill skill : skills) {
                if (skill.getCategory() == null) continue;
                List<JobPost> byCategory = jobPostRepository
                        .findOpenByCategory(skill.getCategory().getId());
                for (JobPost post : byCategory) {
                    if (!addedIds.contains(post.getId())) {
                        addedIds.add(post.getId());
                        result.add(post);
                    }
                    if (result.size() >= 5) break;
                }
                if (result.size() >= 5) break;
            }
        }

        // 2단계: 지역 매칭
        if (result.size() < 5 && loginUser.getActivityRegion() != null
                && !loginUser.getActivityRegion().isBlank()) {
            List<JobPost> byRegion = jobPostRepository
                    .findOpenByRegion(loginUser.getActivityRegion());
            for (JobPost post : byRegion) {
                if (!addedIds.contains(post.getId())) {
                    addedIds.add(post.getId());
                    result.add(post);
                }
                if (result.size() >= 5) break;
            }
        }

        // 3단계: 선호 근무 시간 매칭
        if (result.size() < 5) {
            List<PreferredWorkTime> preferredTimes =
                    preferredWorkTimeRepository.findByUser(loginUser);
            if (!preferredTimes.isEmpty()) {
                List<String> timeTypes = preferredTimes.stream()
                        .map(PreferredWorkTime::getTimeType)
                        .collect(Collectors.toList());
                List<JobPost> byWorkType = jobPostRepository
                        .findOpenByWorkTypes(timeTypes);
                for (JobPost post : byWorkType) {
                    if (!addedIds.contains(post.getId())) {
                        addedIds.add(post.getId());
                        result.add(post);
                    }
                    if (result.size() >= 5) break;
                }
            }
        }

        // 4단계: fallback - 최신 OPEN 5개
        if (result.size() < 5) {
            List<JobPost> latest = jobPostRepository
                    .searchJobPosts(null, null, PostStatus.OPEN, null, null);
            for (JobPost post : latest) {
                if (!addedIds.contains(post.getId())) {
                    addedIds.add(post.getId());
                    result.add(post);
                }
                if (result.size() >= 5) break;
            }
        }

        return result;
    }
}
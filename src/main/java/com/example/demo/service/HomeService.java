package com.example.demo.service;

import com.example.demo.dto.HomeSummaryResponseDto;
import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NotificationRepository notificationRepository;
    private final JobPostRepository jobPostRepository;
    private final WorkSessionRepository workSessionRepository;
    private final PayrollRepository payrollRepository;

    @Transactional(readOnly = true)
    public HomeSummaryResponseDto getSummary(User loginUser) {
        if (loginUser.getRole() == Role.INDIVIDUAL) {
            return getWorkerSummary(loginUser);
        } else {
            return getCompanySummary(loginUser);
        }
    }

    // 근로자용 홈 요약
    private HomeSummaryResponseDto getWorkerSummary(User loginUser) {

        // 지원 현황
        int appliedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.APPLIED);
        int approvedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.APPROVED);
        int rejectedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.REJECTED);

        // 관심 공고 수
        int bookmarkCount = bookmarkRepository.findByUser(loginUser).size();

        // 오늘 근무 수
        String today = LocalDate.now().toString();
        int todayWorkCount = (int) applicationRepository
                .findByUser(loginUser, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPROVED)
                .filter(a -> a.getWorkSession() != null)
                .filter(a -> today.equals(a.getWorkSession().getWorkDate()))
                .count();

        // 미확인 알림 수
        int unreadCount = notificationRepository
                .countByUserAndIsReadFalse(loginUser);

        // 추천 공고 (최신 OPEN 5개)
        List<JobPostResponseDto> recommendedJobPosts = jobPostRepository
                .searchJobPosts(null, null, PostStatus.OPEN, null, null)
                .stream()
                .limit(5)
                .map(post -> new JobPostResponseDto(post, 0))
                .collect(Collectors.toList());

        return HomeSummaryResponseDto.builder()
                .unreadNotificationCount(unreadCount)
                .appliedCount(appliedCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .bookmarkCount(bookmarkCount)
                .todayWorkCount(todayWorkCount)
                .recommendedJobPosts(recommendedJobPosts)
                .build();
    }

    // 기업용 홈 요약
    private HomeSummaryResponseDto getCompanySummary(User loginUser) {

        // 진행 중인 공고 수
        int openJobPostCount = jobPostRepository.findByUser(loginUser)
                .stream()
                .filter(p -> p.getPostStatus() == PostStatus.OPEN)
                .mapToInt(p -> 1)
                .sum();

        // 오늘 Shift 예정 인원
        String today = LocalDate.now().toString();
        int todayShiftWorkerCount = jobPostRepository.findByUser(loginUser)
                .stream()
                .flatMap(jp -> workSessionRepository.findByJobPostAndWorkDate(jp, today).stream())
                .mapToInt(WorkSession::getCurrentCount)
                .sum();

        // 미확인 지원자 수 (APPLIED 상태)
        int pendingApplicantCount = applicationRepository.countByCompanyAndStatus(
                loginUser, ApplicationStatus.APPLIED);

        // 이번 주 정산 총액
        String weekStart = LocalDate.now()
                .with(java.time.DayOfWeek.MONDAY).toString();
        int thisWeekTotalPay = jobPostRepository.findByUser(loginUser)
                .stream()
                .flatMap(jp -> payrollRepository.findByJobPost(jp).stream())
                .filter(p -> p.getWorkWeekStart() != null &&
                             p.getWorkWeekStart().compareTo(weekStart) >= 0)
                .mapToInt(p -> p.getTotalPay())
                .sum();

        // 미확인 알림 수
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
}
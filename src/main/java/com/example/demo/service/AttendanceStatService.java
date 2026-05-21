package com.example.demo.service;

import com.example.demo.dto.AttendanceStatResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceStatService {

    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final JobPostRepository jobPostRepository;
    private final WorkSessionRepository workSessionRepository;

    // Role 기반 자동 분기
    @Transactional(readOnly = true)
    public AttendanceStatResponseDto getStat(User loginUser) {
        if (loginUser.getRole() == Role.INDIVIDUAL) {
            return getWorkerStat(loginUser);
        } else {
            return getCompanyStat(loginUser);
        }
    }

    // 근로자 통계
    private AttendanceStatResponseDto getWorkerStat(User worker) {
        int completed = applicationRepository
                .countByUserAndStatus(worker, ApplicationStatus.COMPLETED);
        int noShow = applicationRepository
                .countByUserAndStatus(worker, ApplicationStatus.NO_SHOW);
        int absent = applicationRepository
                .countByUserAndStatus(worker, ApplicationStatus.ABSENT);

        double totalWorkHours = workAttendanceRepository.findByUser(worker)
                .stream()
                .filter(w -> w.getCheckInTime() != null && w.getCheckOutTime() != null)
                .mapToLong(w -> java.time.Duration.between(
                        w.getCheckInTime(), w.getCheckOutTime()).toMinutes())
                .sum() / 60.0;

        return new AttendanceStatResponseDto(
                completed, noShow, absent,
                worker.getTemperature(),
                totalWorkHours
        );
    }

    // 기업 통계
    private AttendanceStatResponseDto getCompanyStat(User company) {
        int completed = applicationRepository
                .countByCompanyAndStatus(company, ApplicationStatus.COMPLETED);
        int noShow = applicationRepository
                .countByCompanyAndStatus(company, ApplicationStatus.NO_SHOW);
        int absent = applicationRepository
                .countByCompanyAndStatus(company, ApplicationStatus.ABSENT);
        int total = applicationRepository.countByCompany(company);
        int approved = applicationRepository
                .countByCompanyAndStatus(company, ApplicationStatus.APPROVED);
        int rejected = applicationRepository
                .countByCompanyAndStatus(company, ApplicationStatus.REJECTED);

        return new AttendanceStatResponseDto(
                completed, noShow, absent,
                total, approved, rejected
        );
    }

    // 공고별 근태 통계 (기업용)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStatByJobPost(User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        List<JobPost> jobPosts = jobPostRepository.findByUser(loginUser);

        return jobPosts.stream().map(jobPost -> {
            int completed = (int) applicationRepository
                    .findByJobPost(jobPost).stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.COMPLETED)
                    .count();
            int noShow = (int) applicationRepository
                    .findByJobPost(jobPost).stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.NO_SHOW)
                    .count();
            int absent = (int) applicationRepository
                    .findByJobPost(jobPost).stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.ABSENT)
                    .count();
            int total = completed + noShow + absent;

            double attendanceRate = total > 0
                    ? Math.round((completed / (double) total) * 1000) / 10.0 : 0.0;

            return Map.<String, Object>of(
                    "jobPostId", jobPost.getId(),
                    "jobPostTitle", jobPost.getTitle(),
                    "completedCount", completed,
                    "noShowCount", noShow,
                    "absentCount", absent,
                    "attendanceRate", attendanceRate
            );
        }).collect(Collectors.toList());
    }

    // 월별 근태 통계 (근로자용)
    @Transactional(readOnly = true)
    public Map<String, Object> getWorkerMonthlystat(
            User loginUser, int year, int month) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("개인 회원만 조회 가능합니다.");
        }

        java.time.LocalDateTime startOfMonth =
                java.time.LocalDate.of(year, month, 1).atStartOfDay();
        java.time.LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<WorkAttendance> attendances =
                workAttendanceRepository.findByUserAndMonth(
                        loginUser, startOfMonth, endOfMonth);

        int workDays = attendances.stream()
                .map(a -> a.getCheckInTime().toLocalDate())
                .collect(Collectors.toSet()).size();

        double totalWorkHours = attendances.stream()
                .filter(w -> w.getCheckInTime() != null && w.getCheckOutTime() != null)
                .mapToLong(w -> java.time.Duration.between(
                        w.getCheckInTime(), w.getCheckOutTime()).toMinutes())
                .sum() / 60.0;

        return Map.of(
                "year", year,
                "month", month,
                "workDays", workDays,
                "totalWorkHours", totalWorkHours
        );
    }

    // 구직자 캘린더 (근무 확정 날짜 목록)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWorkerCalendar(User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("구직자만 조회 가능합니다.");
        }

        return applicationRepository.findByUser(loginUser,
                org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(a -> a.getStatus() == ApplicationStatus.APPROVED
                        || a.getStatus() == ApplicationStatus.COMPLETED)
                .filter(a -> a.getWorkSession() != null)
                .map(a -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", a.getWorkSession().getWorkDate());
                    map.put("jobPostTitle", a.getJobPost().getTitle());
                    map.put("jobPostId", a.getJobPost().getId());
                    map.put("startTime", a.getWorkSession().getStartTime());
                    map.put("endTime", a.getWorkSession().getEndTime());
                    map.put("status", a.getStatus().name());
                    map.put("workLocation", a.getJobPost().getWorkLocation());
                    return map;
                })
                .sorted(Comparator.comparing(m -> (String) m.get("date")))
                .collect(Collectors.toList());
    }

    // 기업 캘린더 (공고별 Shift 일정)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCompanyCalendar(
            Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 조회 가능합니다.");
        }

        return workSessionRepository.findByJobPost(jobPost)
                .stream()
                .map(ws -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("date", ws.getWorkDate());
                    map.put("workSessionId", ws.getId());
                    map.put("startTime", ws.getStartTime());
                    map.put("endTime", ws.getEndTime());
                    map.put("recruitCount", ws.getRecruitCount());
                    map.put("currentCount", ws.getCurrentCount());
                    map.put("status", ws.getStatus().name());
                    map.put("memo", ws.getMemo());
                    return map;
                })
                .sorted(Comparator.comparing(m -> (String) m.get("date")))
                .collect(Collectors.toList());
    }
}
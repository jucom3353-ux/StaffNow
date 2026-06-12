package com.example.demo.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AttendanceStatResponseDto;
import com.example.demo.entity.ApplicationStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkAttendance;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import com.example.demo.repository.WorkSessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceStatService {

    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final JobPostRepository jobPostRepository;
    private final WorkSessionRepository workSessionRepository;

    @Transactional(readOnly = true)
    public AttendanceStatResponseDto getStat(User loginUser) {
        if (loginUser.getRole() == Role.INDIVIDUAL) {
            return getWorkerStat(loginUser);
        } else {
            return getCompanyStat(loginUser);
        }
    }

    private AttendanceStatResponseDto getWorkerStat(User worker) {
        int completed = applicationRepository.countByUserAndStatus(worker, ApplicationStatus.COMPLETED);
        int noShow = applicationRepository.countByUserAndStatus(worker, ApplicationStatus.NO_SHOW);
        int absent = applicationRepository.countByUserAndStatus(worker, ApplicationStatus.ABSENT);

        double totalWorkHours = workAttendanceRepository.findByUser(worker)
                .stream()
                .filter(w -> w.getCheckInTime() != null && w.getCheckOutTime() != null)
                .mapToLong(w -> java.time.Duration.between(
                        w.getCheckInTime(), w.getCheckOutTime()).toMinutes())
                .sum() / 60.0;

        return new AttendanceStatResponseDto(completed, noShow, absent,
                worker.getTemperature(), totalWorkHours);
    }

    private AttendanceStatResponseDto getCompanyStat(User company) {
        int completed = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.COMPLETED);
        int noShow = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.NO_SHOW);
        int absent = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.ABSENT);
        int total = applicationRepository.countByCompany(company);
        int approved = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.APPROVED);
        int rejected = applicationRepository.countByCompanyAndStatus(company, ApplicationStatus.REJECTED);

        return new AttendanceStatResponseDto(completed, noShow, absent, total, approved, rejected);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStatByJobPost(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        return jobPostRepository.findByUser(loginUser).stream().map(jobPost -> {
            int completed = (int) applicationRepository.findByJobPost(jobPost).stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.COMPLETED).count();
            int noShow = (int) applicationRepository.findByJobPost(jobPost).stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.NO_SHOW).count();
            int absent = (int) applicationRepository.findByJobPost(jobPost).stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.ABSENT).count();
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

    @Transactional(readOnly = true)
    public Map<String, Object> getWorkerMonthlystat(User loginUser, int year, int month) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        java.time.LocalDateTime startOfMonth =
                java.time.LocalDate.of(year, month, 1).atStartOfDay();
        java.time.LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        List<WorkAttendance> attendances =
                workAttendanceRepository.findByUserAndMonth(loginUser, startOfMonth, endOfMonth);

        int workDays = attendances.stream()
                .map(a -> a.getCheckInTime().toLocalDate())
                .collect(Collectors.toSet()).size();

        double totalWorkHours = attendances.stream()
                .filter(w -> w.getCheckInTime() != null && w.getCheckOutTime() != null)
                .mapToLong(w -> java.time.Duration.between(
                        w.getCheckInTime(), w.getCheckOutTime()).toMinutes())
                .sum() / 60.0;

        return Map.of("year", year, "month", month,
                "workDays", workDays, "totalWorkHours", totalWorkHours);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWorkerCalendar(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCompanyCalendar(Long jobPostId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        return workSessionRepository.findByJobPost(jobPost).stream()
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
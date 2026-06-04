package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final ApplicationRepository applicationRepository;
    private final CareerRepository careerRepository;
    private final ResumeRepository resumeRepository;

    // 대표님 확정 후 수치 변경
    private static final int STAFF_MIN_COUNT = 10;
    private static final int PRO_MIN_COUNT = 30;
    private static final int PROMOTER_MIN_COUNT = 50;

    private static final int STAFF_MIN_MONTHS = 6;
    private static final int PRO_MIN_MONTHS = 18;
    private static final int PROMOTER_MIN_MONTHS = 36;

    @Transactional
    public void updateGrade(User user) {
        if (user.getRole() != Role.INDIVIDUAL) return;

        int completedCount = applicationRepository
                .countByUserAndStatus(user, ApplicationStatus.COMPLETED);

        int totalCareerMonths = getTotalCareerMonths(user);

        String newGrade = calculateGrade(completedCount, totalCareerMonths);

        if (!newGrade.equals(user.getGrade())) {
            user.setGrade(newGrade);
        }
    }

    private String calculateGrade(int completedCount, int totalCareerMonths) {
        if (completedCount >= PROMOTER_MIN_COUNT
                || totalCareerMonths >= PROMOTER_MIN_MONTHS) {
            return "프로모터";
        }
        if (completedCount >= PRO_MIN_COUNT
                || totalCareerMonths >= PRO_MIN_MONTHS) {
            return "프로";
        }
        if (completedCount >= STAFF_MIN_COUNT
                || totalCareerMonths >= STAFF_MIN_MONTHS) {
            return "스탭";
        }
        return "아마추어";
    }

    private int getTotalCareerMonths(User user) {
        int inAppMonths = getInAppCareerMonths(user);
        int resumeMonths = getResumeCareerMonths(user);
        return inAppMonths + resumeMonths;
    }

    private int getInAppCareerMonths(User user) {
        return applicationRepository
                .findFirstByUserAndStatusOrderByCreatedAtAsc(
                        user, ApplicationStatus.COMPLETED)
                .map(app -> {
                    LocalDateTime start = app.getCreatedAt();
                    LocalDateTime now = LocalDateTime.now();
                    return (int) ChronoUnit.MONTHS.between(start, now);
                })
                .orElse(0);
    }

    private int getResumeCareerMonths(User user) {
        return resumeRepository.findByUser(user)
                .map(resume -> {
                    List<Career> careers = careerRepository.findByResume(resume);
                    return careers.stream()
                            .mapToInt(career -> {
                                if (career.getJoinDate() == null) return 0;
                                try {
                                    // "yyyy-MM" 형식 파싱
                                    DateTimeFormatter formatter =
                                            DateTimeFormatter.ofPattern("yyyy-MM");
                                    LocalDate start = LocalDate.parse(
                                            career.getJoinDate() + "-01",
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                                    LocalDate end;
                                    if (Boolean.TRUE.equals(career.getIsCurrent())
                                            || career.getLeaveDate() == null) {
                                        end = LocalDate.now();
                                    } else {
                                        end = LocalDate.parse(
                                                career.getLeaveDate() + "-01",
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                    }
                                    return (int) ChronoUnit.MONTHS.between(start, end);
                                } catch (Exception e) {
                                    return 0;
                                }
                            })
                            .sum();
                })
                .orElse(0);
    }

    public String getGradeDescription(String grade) {
        return switch (grade) {
            case "스탭" -> "근무 완료 10회 이상 또는 경력 6개월 이상";
            case "프로" -> "근무 완료 30회 이상 또는 경력 18개월 이상";
            case "프로모터" -> "근무 완료 50회 이상 또는 경력 36개월 이상";
            default -> "StaffNow 신규 회원";
        };
    }
}
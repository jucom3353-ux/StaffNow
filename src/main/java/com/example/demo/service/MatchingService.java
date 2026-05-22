package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CareerRepository careerRepository;
    private final ResumeRepository resumeRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final WorkAttendanceRepository workAttendanceRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> autoMatch(Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        int matchingLimit = getMatchingLimit(loginUser);
        if (matchingLimit == 0) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_REQUIRED);
        }

        JobCategory jobCategory = jobPost.getCategory();
        if (jobCategory == null) {
            throw new CustomException(ErrorCode.CATEGORY_REQUIRED);
        }

        String jobRegion = jobPost.getWorkLocation() != null
                && jobPost.getWorkLocation().length() >= 2
                ? jobPost.getWorkLocation().substring(0, 2) : null;

        List<User> allWorkers = userRepository.findByRole(Role.INDIVIDUAL)
                .stream()
                .filter(w -> !Boolean.TRUE.equals(w.getSuspended()))
                .collect(Collectors.toList());

        if (allWorkers.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Skill>> skillMap = skillRepository.findByUserIn(allWorkers)
                .stream()
                .collect(Collectors.groupingBy(s -> s.getUser().getId()));

        Map<Long, Resume> resumeMap = resumeRepository.findByUserIn(allWorkers)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getUser().getId(),
                        r -> r,
                        (a, b) -> a
                ));

        List<Resume> allResumes = new ArrayList<>(resumeMap.values());

        Map<Long, Boolean> hasCareerMap = allResumes.isEmpty()
                ? Collections.emptyMap()
                : careerRepository.findByResumeIn(allResumes)
                        .stream()
                        .collect(Collectors.toMap(
                                c -> c.getResume().getId(),
                                c -> true,
                                (a, b) -> true
                        ));

        Map<Long, List<Career>> careerMap = allResumes.isEmpty()
                ? Collections.emptyMap()
                : careerRepository.findByResumeIn(allResumes)
                        .stream()
                        .collect(Collectors.groupingBy(c -> c.getResume().getId()));

        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        Set<Long> recentlyWorkedIds = new HashSet<>(
                workAttendanceRepository.findRecentlyWorkedUserIds(allWorkers, ninetyDaysAgo)
        );

        List<Map<String, Object>> scored = new ArrayList<>();

        for (User worker : allWorkers) {

            int score = 0;

            // 1. 카테고리 매칭 - 40점
            List<Skill> skills = skillMap.getOrDefault(worker.getId(), Collections.emptyList());
            boolean categoryMatch = skills.stream()
                    .anyMatch(s -> s.getCategory() != null
                            && s.getCategory().getId().equals(jobCategory.getId()));
            if (!categoryMatch && jobCategory.getParent() != null) {
                categoryMatch = skills.stream()
                        .anyMatch(s -> s.getCategory() != null
                                && s.getCategory().getId()
                                .equals(jobCategory.getParent().getId()));
            }
            if (!categoryMatch) continue;
            score += 40;

            // 2. 경력 보유 여부 - 20점
            Resume resume = resumeMap.get(worker.getId());
            boolean hasCareer = resume != null
                    && Boolean.TRUE.equals(hasCareerMap.get(resume.getId()));
            if (hasCareer) score += 20;

            // 3. 경력 연차 - 최대 15점 (1년당 +3)
            if (hasCareer && resume != null) {
                List<Career> careers = careerMap.getOrDefault(
                        resume.getId(), Collections.emptyList());
                int totalMonths = careers.stream()
                        .mapToInt(c -> {
                            if (c.getJoinDate() == null) return 0;
                            try {
                                DateTimeFormatter fmt =
                                        DateTimeFormatter.ofPattern("yyyy-MM");
                                YearMonth start = YearMonth.parse(c.getJoinDate(), fmt);
                                YearMonth end = (c.getLeaveDate() != null
                                        && !c.getLeaveDate().isBlank())
                                        ? YearMonth.parse(c.getLeaveDate(), fmt)
                                        : YearMonth.now();
                                return (int) ChronoUnit.MONTHS.between(start, end);
                            } catch (Exception e) {
                                return 0;
                            }
                        })
                        .sum();
                int careerScore = Math.min((totalMonths / 12) * 3, 15);
                score += careerScore;
            }

            // 4. 지역 매칭 - 20점
            if (jobRegion != null && worker.getActivityRegion() != null
                    && worker.getActivityRegion().contains(jobRegion)) {
                score += 20;
            }

            // 5. 상시근무 가능 - 10점
            if (Boolean.TRUE.equals(worker.getAvailableAlways())) {
                score += 10;
            }

            // 6. 최근 90일 내 실제 근무 이력 - 10점
            if (recentlyWorkedIds.contains(worker.getId())) {
                score += 10;
            }

            // 7. 이력서 완성도 - 5점
            if (resume != null
                    && resume.getDesiredJob() != null
                    && !resume.getDesiredJob().isBlank()
                    && resume.getDesiredLocation() != null
                    && !resume.getDesiredLocation().isBlank()) {
                score += 5;
            }

            double finalScore = score + (worker.getTemperature() != null
                    ? worker.getTemperature() / 1000.0 : 0);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("workerId", worker.getId());
            result.put("name", worker.getName());
            result.put("activityRegion", worker.getActivityRegion());
            result.put("temperature", worker.getTemperature());
            result.put("availableAlways", worker.getAvailableAlways());
            result.put("matchScore", score);
            result.put("categoryMatch", true);
            result.put("hasCareer", hasCareer);
            result.put("recentlyWorked", recentlyWorkedIds.contains(worker.getId()));
            scored.add(Map.of("score", finalScore, "data", result));
        }

        return scored.stream()
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("score"), (Double) a.get("score")))
                .limit(matchingLimit)
                .map(m -> (Map<String, Object>) m.get("data"))
                .collect(Collectors.toList());
    }

    private int getMatchingLimit(User loginUser) {
        return companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .map(sub -> sub.getPlan().getMatchingLimit() != null
                        ? sub.getPlan().getMatchingLimit() : 0)
                .orElse(0);
    }
}
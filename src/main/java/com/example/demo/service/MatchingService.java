package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.Career;
import com.example.demo.entity.JobCategory;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.Resume;
import com.example.demo.entity.Role;
import com.example.demo.entity.Skill;
import com.example.demo.entity.SubscriptionStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CareerRepository;
import com.example.demo.repository.CompanySubscriptionRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.PreferredCategoryRepository;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.SkillRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkAttendanceRepository;
import com.example.demo.util.AuthorizationUtil;

import lombok.RequiredArgsConstructor;

@Service
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class MatchingService {

    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final CareerRepository careerRepository;
    private final ResumeRepository resumeRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final PreferredCategoryRepository preferredCategoryRepository;
    private final ApplicationRepository applicationRepository;

    // ===== 기업/매니저 → 구직자 추천 =====
    @Transactional(readOnly = true)
    public List<Map<String, Object>> autoMatch(Long jobPostId, User loginUser) {

        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!AuthorizationUtil.isMyJobPost(jobPost, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        // 완료된 공고는 매칭 불가
        if (jobPost.getPostStatus() == PostStatus.CLOSED) {
        throw new CustomException(ErrorCode.JOB_POST_CLOSED);
        }

        int matchingLimit = getMatchingLimit(
                AuthorizationUtil.getCompanyUser(loginUser));
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

        if (allWorkers.isEmpty()) return Collections.emptyList();

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
                        .collect(Collectors.groupingBy(
                                c -> c.getResume().getId()));

        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        Set<Long> recentlyWorkedIds = new HashSet<>(
                workAttendanceRepository.findRecentlyWorkedUserIds(
                        allWorkers, ninetyDaysAgo));

        List<Map<String, Object>> scored = new ArrayList<>();

        for (User worker : allWorkers) {

            int score = 0;

            // 1. 카테고리 매칭 - 40점 (필수)
            List<Skill> skills = skillMap.getOrDefault(
                    worker.getId(), Collections.emptyList());
            boolean categoryMatch = skills.stream()
                    .anyMatch(s -> s.getCategory() != null
                            && s.getCategory().getId().equals(jobCategory.getId()));
            if (!categoryMatch && jobCategory.getParent() != null) {
                categoryMatch = skills.stream()
                        .anyMatch(s -> s.getCategory() != null
                                && s.getCategory().getId()
                                .equals(jobCategory.getParent().getId()));
            }

            // 선호 카테고리도 카테고리 매칭 기준에 포함
            if (!categoryMatch) {
                List<Long> preferredIds = preferredCategoryRepository
                        .findCategoryIdsByUser(worker);
                categoryMatch = preferredIds.contains(jobCategory.getId())
                        || (jobCategory.getParent() != null
                                && preferredIds.contains(
                                        jobCategory.getParent().getId()));
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
                                YearMonth start = YearMonth.parse(
                                        c.getJoinDate(), fmt);
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
                score += Math.min((totalMonths / 12) * 3, 15);
            }

            // 4. 지역 매칭 - 20점
            if (jobRegion != null && worker.getActivityRegion() != null
                    && worker.getActivityRegion().contains(jobRegion)) {
                score += 20;
            }

            // 5. 상시근무 가능 - 10점
            if (Boolean.TRUE.equals(worker.getAvailableAlways())) score += 10;

            // 6. 최근 90일 내 실제 근무 이력 - 10점
            if (recentlyWorkedIds.contains(worker.getId())) score += 10;

            // 7. 이력서 완성도 - 5점
            if (resume != null
                    && resume.getDesiredJob() != null
                    && !resume.getDesiredJob().isBlank()
                    && resume.getDesiredLocation() != null
                    && !resume.getDesiredLocation().isBlank()) {
                score += 5;
            }

            // 타이브레이커: 등급 점수
            double finalScore = score + (worker.getGradeScore() != null
                    ? worker.getGradeScore() / 100.0 : 0);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("workerId", worker.getId());
            result.put("name", worker.getName());
            result.put("activityRegion", worker.getActivityRegion());
            result.put("gradeScore", worker.getGradeScore());
            result.put("grade", worker.getGrade());
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

    // ===== 구직자 → 공고 추천 =====
    @Transactional(readOnly = true)
    public List<JobPostResponseDto> recommendJobPostsForWorker(User loginUser) {

        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        // 구직자 선호 카테고리 ID 목록
        List<Long> preferredCategoryIds = preferredCategoryRepository
                .findCategoryIdsByUser(loginUser);

        if (preferredCategoryIds.isEmpty()) {
            // 선호 카테고리 없으면 전체 OPEN 공고 최신순 반환
            return jobPostRepository
                    .findByPostStatus(PostStatus.OPEN)
                    .stream()
                    .map(p -> new JobPostResponseDto(p,
                            applicationRepository.countByJobPost(p)))
                    .collect(Collectors.toList());
        }

        // 이미 지원한 공고 ID 목록 (완료 포함 제외)
        Set<Long> appliedJobPostIds = applicationRepository
                .findByUser(loginUser)
                .stream()
                .map(a -> a.getJobPost().getId())
                .collect(Collectors.toSet());

        // 선호 카테고리 기반 OPEN 공고 조회 + 스코어링
        List<Map<String, Object>> scored = new ArrayList<>();

        for (Long categoryId : preferredCategoryIds) {
            jobPostRepository.findOpenByCategory(categoryId)
                    .stream()
                    .filter(p -> !appliedJobPostIds.contains(p.getId()))
                    .forEach(post -> {
                        int score = 0;

                        // 카테고리 매칭 - 40점
                        score += 40;

                        // 지역 매칭 - 30점
                        if (loginUser.getActivityRegion() != null
                                && post.getWorkLocation() != null
                                && post.getWorkLocation().contains(
                                        loginUser.getActivityRegion()
                                                .substring(0, Math.min(
                                                        loginUser.getActivityRegion().length(), 2)))) {
                            score += 30;
                        }

                        // 긴급 공고 - 20점
                        if (Boolean.TRUE.equals(post.getUrgentBadge())) score += 20;

                        // 마감 임박 (3일 이내) - 10점
                        if (post.getDeadline() != null) {
                            try {
                                java.time.LocalDate deadline =
                                        java.time.LocalDate.parse(post.getDeadline());
                                long daysLeft = java.time.temporal.ChronoUnit.DAYS
                                        .between(java.time.LocalDate.now(), deadline);
                                if (daysLeft >= 0 && daysLeft <= 3) score += 10;
                            } catch (Exception ignored) {}
                        }

                        scored.add(Map.of(
                                "score", (double) score,
                                "data", post
                        ));
                    });
        }

        return scored.stream()
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("score"), (Double) a.get("score")))
                .map(m -> (JobPost) m.get("data"))
                .distinct()
                .map(p -> new JobPostResponseDto(p,
                        applicationRepository.countByJobPost(p)))
                .collect(Collectors.toList());
    }

    private int getMatchingLimit(User companyUser) {
        return companySubscriptionRepository
                .findByCompanyAndStatus(companyUser, SubscriptionStatus.ACTIVE)
                .map(sub -> sub.getPlan().getMatchingLimit() != null
                        ? sub.getPlan().getMatchingLimit() : 0)
                .orElse(0);
    }
}
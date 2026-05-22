package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
            throw new RuntimeException("기업 회원만 자동매칭 가능합니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고만 자동매칭 가능합니다.");
        }

        int matchingLimit = getMatchingLimit(loginUser);
        if (matchingLimit == 0) {
            throw new RuntimeException("자동매칭은 구독 플랜이 필요합니다.");
        }

        JobCategory jobCategory = jobPost.getCategory();
        if (jobCategory == null) {
            throw new RuntimeException("공고에 카테고리가 설정되어 있지 않습니다.");
        }

        String jobRegion = jobPost.getWorkLocation() != null
                && jobPost.getWorkLocation().length() >= 2
                ? jobPost.getWorkLocation().substring(0, 2) : null;

        // 정지 유저 필터링
        List<User> allWorkers = userRepository.findByRole(Role.INDIVIDUAL)
                .stream()
                .filter(w -> !Boolean.TRUE.equals(w.getSuspended()))
                .collect(Collectors.toList());

        if (allWorkers.isEmpty()) {
            return Collections.emptyList();
        }

        // ✅ 배치 조회 - N+1 제거
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

        // ✅ 경력 연차 계산용 careerMap (resumeId → List<Career>)
        Map<Long, List<Career>> careerMap = allResumes.isEmpty()
                ? Collections.emptyMap()
                : careerRepository.findByResumeIn(allResumes)
                        .stream()
                        .collect(Collectors.groupingBy(c -> c.getResume().getId()));

        // ✅ 최근 90일 내 실제 근무 완료한 워커 ID
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        Set<Long> recentlyWorkedIds = new HashSet<>(
                workAttendanceRepository.findRecentlyWorkedUserIds(allWorkers, ninetyDaysAgo)
        );

        // 스코어링
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

            // 7. 이력서 완성도 - 5점 (희망직종 + 희망근무지 둘 다 작성)
            if (resume != null
                    && resume.getDesiredJob() != null
                    && !resume.getDesiredJob().isBlank()
                    && resume.getDesiredLocation() != null
                    && !resume.getDesiredLocation().isBlank()) {
                score += 5;
            }

            // temperature 보정 (동점자 처리)
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

        // 점수 높은 순 정렬 → 플랜별 인원 제한
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
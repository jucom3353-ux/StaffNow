package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 자동매칭 실행
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

        // 구독 플랜 확인 → 매칭 인원 제한
        int matchingLimit = getMatchingLimit(loginUser);
        if (matchingLimit == 0) {
            throw new RuntimeException("자동매칭은 구독 플랜이 필요합니다.");
        }

        // 공고 카테고리
        JobCategory jobCategory = jobPost.getCategory();
        if (jobCategory == null) {
            throw new RuntimeException("공고에 카테고리가 설정되어 있지 않습니다.");
        }

        // 공고 지역 (workLocation 앞 2글자로 매칭 - 예: "광주")
        String jobRegion = jobPost.getWorkLocation() != null
                && jobPost.getWorkLocation().length() >= 2
                ? jobPost.getWorkLocation().substring(0, 2) : null;

        // 전체 구직자 조회
        List<User> allWorkers = userRepository.findByRole(Role.INDIVIDUAL);

        // 매칭 점수 계산
        List<Map<String, Object>> scored = new ArrayList<>();

        for (User worker : allWorkers) {

            // 정지된 유저 제외
            if (Boolean.TRUE.equals(worker.getSuspended())) continue;

            int score = 0;

            // 1. 카테고리 매칭 (스킬 기준) - 40점
            List<Skill> skills = skillRepository.findByUser(worker);
            boolean categoryMatch = skills.stream()
                    .anyMatch(s -> s.getCategory() != null
                            && s.getCategory().getId().equals(jobCategory.getId()));
            // 부모 카테고리도 체크
            if (!categoryMatch && jobCategory.getParent() != null) {
                categoryMatch = skills.stream()
                        .anyMatch(s -> s.getCategory() != null
                                && s.getCategory().getId()
                                .equals(jobCategory.getParent().getId()));
            }
            if (!categoryMatch) continue; // 카테고리 미매칭 제외
            score += 40;

            // 2. 경력 보유 여부 - 30점
            Resume resume = resumeRepository.findByUser(worker).orElse(null);
            if (resume != null) {
                List<Career> careers = careerRepository.findByResume(resume);
                if (!careers.isEmpty()) score += 30;
            }

            // 3. 지역 매칭 - 20점
            if (jobRegion != null && worker.getActivityRegion() != null
                    && worker.getActivityRegion().contains(jobRegion)) {
                score += 20;
            }

            // 4. 상시근무 가능 - 10점
            if (Boolean.TRUE.equals(worker.getAvailableAlways())) {
                score += 10;
            }

            // temperature 보정 (소수점 점수로 동점자 처리)
            double finalScore = score + (worker.getTemperature() != null
                    ? worker.getTemperature() / 1000.0 : 0);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("workerId", worker.getId());
            result.put("name", worker.getName());
            result.put("activityRegion", worker.getActivityRegion());
            result.put("temperature", worker.getTemperature());
            result.put("availableAlways", worker.getAvailableAlways());
            result.put("matchScore", score);
            result.put("categoryMatch", categoryMatch);
            result.put("hasCareer", resume != null
                    && !careerRepository.findByResume(resume).isEmpty());
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

    // 구독 플랜별 매칭 인원 반환
    private int getMatchingLimit(User loginUser) {
        return companySubscriptionRepository
                .findByCompanyAndStatus(loginUser, SubscriptionStatus.ACTIVE)
                .map(sub -> sub.getPlan().getMatchingLimit() != null
                        ? sub.getPlan().getMatchingLimit() : 0)
                .orElse(0);
    }
}
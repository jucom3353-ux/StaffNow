package com.example.demo.service;

import com.example.demo.dto.JobPostResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobPostRecommendService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final JobPostViewHistoryRepository jobPostViewHistoryRepository;
    private final PreferredWorkTimeRepository preferredWorkTimeRepository;

    @Transactional(readOnly = true)
    public List<JobPostResponseDto> recommend(User loginUser, int limit) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            return List.of();
        }

        // 현재 OPEN 공고 전체
        List<JobPost> openPosts = jobPostRepository.findByPostStatus(PostStatus.OPEN);

        // 이미 지원한 공고 ID
        Set<Long> appliedIds = applicationRepository
                .findByUser(loginUser, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(a -> a.getJobPost() != null ? a.getJobPost().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 최근 본 공고 카테고리 ID
        Set<Long> viewedCategoryIds = jobPostViewHistoryRepository
                .findByUserOrderByViewedAtDesc(loginUser)
                .stream()
                .limit(10)
                .map(h -> h.getJobPost().getCategory())
                .filter(Objects::nonNull)
                .map(JobCategory::getId)
                .collect(Collectors.toSet());

        // 최근 지원한 공고 카테고리 ID
        Set<Long> appliedCategoryIds = applicationRepository
                .findByUser(loginUser, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(a -> a.getJobPost().getCategory())
                .filter(Objects::nonNull)
                .map(JobCategory::getId)
                .collect(Collectors.toSet());

        // 선호 근무 시간대
        Set<String> preferredTimeTypes = preferredWorkTimeRepository
                .findByUser(loginUser)
                .stream()
                .map(PreferredWorkTime::getTimeType)
                .collect(Collectors.toSet());

        // 점수 계산
        Map<JobPost, Integer> scoreMap = new LinkedHashMap<>();

        for (JobPost post : openPosts) {
            // 이미 지원한 공고 제외
            if (appliedIds.contains(post.getId())) continue;

            int score = 0;

            // 1. 선호 지역 일치
            if (loginUser.getActivityRegion() != null
                    && post.getWorkLocation() != null
                    && post.getWorkLocation().contains(loginUser.getActivityRegion())) {
                score += 30;
            }

            // 2. 최근 지원한 공고 카테고리 일치
            if (post.getCategory() != null
                    && appliedCategoryIds.contains(post.getCategory().getId())) {
                score += 25;
            }

            // 3. 최근 본 공고 카테고리 일치
            if (post.getCategory() != null
                    && viewedCategoryIds.contains(post.getCategory().getId())) {
                score += 20;
            }

            // 4. 선호 근무 시간대 일치
            if (post.getStartTime() != null && !preferredTimeTypes.isEmpty()) {
                for (String timeType : preferredTimeTypes) {
                    if (matchesTimeType(post.getStartTime(), timeType)) {
                        score += 15;
                        break;
                    }
                }
            }

            // 5. 긴급 공고 보너스
            if (Boolean.TRUE.equals(post.getUrgentBadge())) {
                score += 10;
            }

            // 6. 상단 노출 공고 보너스
            if (Boolean.TRUE.equals(post.getTopExposure())) {
                score += 5;
            }

            scoreMap.put(post, score);
        }

        // 점수 내림차순 정렬 후 limit 적용
        return scoreMap.entrySet().stream()
                .sorted(Map.Entry.<JobPost, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> new JobPostResponseDto(e.getKey(),
                        applicationRepository.countByJobPost(e.getKey())))
                .collect(Collectors.toList());
    }

    // 시간대 매칭 (AM: 06~12, PM: 12~18, EVENING: 18~22, NIGHT: 22~06)
    private boolean matchesTimeType(String startTime, String timeType) {
        try {
            int hour = Integer.parseInt(startTime.split(":")[0]);
            return switch (timeType.toUpperCase()) {
                case "AM" -> hour >= 6 && hour < 12;
                case "PM" -> hour >= 12 && hour < 18;
                case "EVENING" -> hour >= 18 && hour < 22;
                case "NIGHT" -> hour >= 22 || hour < 6;
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }
}
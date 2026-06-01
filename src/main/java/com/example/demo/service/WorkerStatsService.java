package com.example.demo.service;

import com.example.demo.dto.WorkerStatsResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerStatsService {

    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional(readOnly = true)
    public WorkerStatsResponseDto getMyStats(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        // 지원 현황
        long totalApplications = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.APPLIED)
                + applicationRepository.countByUserAndStatus(loginUser, ApplicationStatus.APPROVED)
                + applicationRepository.countByUserAndStatus(loginUser, ApplicationStatus.COMPLETED)
                + applicationRepository.countByUserAndStatus(loginUser, ApplicationStatus.NO_SHOW)
                + applicationRepository.countByUserAndStatus(loginUser, ApplicationStatus.REJECTED);

        long approvedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.APPROVED);
        long completedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.COMPLETED);
        long noShowCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.NO_SHOW);
        long rejectedCount = applicationRepository
                .countByUserAndStatus(loginUser, ApplicationStatus.REJECTED);

        double completionRate = (approvedCount + completedCount) > 0
                ? (double) completedCount / (approvedCount + completedCount) * 100 : 0.0;
        double noShowRate = (approvedCount + completedCount) > 0
                ? (double) noShowCount / (approvedCount + completedCount) * 100 : 0.0;

        // 리뷰
        List<Review> reviews = reviewRepository.findByWorker(loginUser);
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating).average().orElse(0);
        double avgSincerity = reviews.stream()
                .mapToInt(Review::getSincerityRating).average().orElse(0);
        double avgKindness = reviews.stream()
                .mapToInt(Review::getKindnessRating).average().orElse(0);
        double avgSkill = reviews.stream()
                .mapToInt(Review::getSkillRating).average().orElse(0);

        // 북마크
        long bookmarkCount = bookmarkRepository.findByUser(loginUser).size();

        return WorkerStatsResponseDto.builder()
                .totalApplications(totalApplications)
                .approvedCount(approvedCount)
                .completedCount(completedCount)
                .noShowCount(noShowCount)
                .rejectedCount(rejectedCount)
                .completionRate(Math.round(completionRate * 10.0) / 10.0)
                .noShowRate(Math.round(noShowRate * 10.0) / 10.0)
                .averageRating(Math.round(averageRating * 10.0) / 10.0)
                .avgSincerityRating(Math.round(avgSincerity * 10.0) / 10.0)
                .avgKindnessRating(Math.round(avgKindness * 10.0) / 10.0)
                .avgSkillRating(Math.round(avgSkill * 10.0) / 10.0)
                .reviewCount(reviews.size())
                .temperature(loginUser.getTemperature() != null ? loginUser.getTemperature() : 36.5)
                .mileage(loginUser.getMileage())
                .bookmarkCount(bookmarkCount)
                .build();
    }
}
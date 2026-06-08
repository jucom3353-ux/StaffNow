package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradeService {

    private final ApplicationRepository applicationRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    // 등급 기준 점수
    private static final double STAFF_MAX     = 3.0;
    private static final double PRO_MIN       = 3.1;
    private static final double PRO_MAX       = 4.9;
    private static final double PROMOTER_MIN  = 5.0;

    // 초기 혜택: 처음 3건은 건당 최대 0.1 (기업 평가 점수 × 0.1)
    private static final int    EARLY_BENEFIT_COUNT   = 3;
    private static final double EARLY_BENEFIT_MAX     = 1.0;

    // 4건 이후: 건당 기본 0.05 + 별점 1개당 0.01
    private static final double NORMAL_BASE_SCORE     = 0.05;
    private static final double STAR_SCORE_PER_POINT  = 0.01;

    /**
     * 기업이 리뷰를 남긴 후 호출
     * 노쇼/지각 처리 후에도 호출 (AttendanceStatus 기반)
     */
    @Transactional
    public void applyReviewScore(User worker, Application application,
                                  Review review, AttendanceStatus attendanceStatus) {
        if (worker.getRole() != Role.INDIVIDUAL) return;

        // 노쇼: 점수 0으로 초기화
        if (attendanceStatus == AttendanceStatus.ABSENT) {
            worker.setGradeScore(0.0);
            worker.setGrade(calculateGrade(0.0));
            userRepository.save(worker);
            log.info("[등급] 노쇼로 점수 초기화 userId={}", worker.getId());
            return;
        }

        // 완료된 근무 횟수 (리뷰 있는 건만 카운트)
        List<Review> companyReviews = reviewRepository
                .findByWorkerAndReviewType(worker, ReviewType.COMPANY_TO_WORKER);
        int reviewedCount = companyReviews.size(); // 현재 리뷰 포함 전 카운트

        double addedScore;

        if (reviewedCount < EARLY_BENEFIT_COUNT) {
            // 초기 혜택: rating 1~5 기준, 건당 최대 0.1 (× 0.02)
            addedScore = Math.min(review.getRating() * 0.02, 0.1);
        } else {
            // 지각: 기본 0.05 미적용, 별점 점수만 적용 (rating 1~5 × 0.01)
            if (attendanceStatus == AttendanceStatus.LATE) {
                addedScore = review.getRating() * STAR_SCORE_PER_POINT;
            } else {
                addedScore = NORMAL_BASE_SCORE
                        + review.getRating() * STAR_SCORE_PER_POINT;
            }
        }

        double newScore = Math.round(
                (worker.getGradeScore() + addedScore) * 100.0) / 100.0;

        worker.setGradeScore(newScore);
        worker.setGrade(calculateGrade(newScore));
        userRepository.save(worker);

        log.info("[등급] 점수 갱신 userId={}, addedScore={}, newScore={}, grade={}",
                worker.getId(), addedScore, newScore, worker.getGrade());
    }

    /**
     * 노쇼 발생 시 단독 호출 (리뷰 없이 노쇼 처리 시)
     */
    @Transactional
    public void applyNoShow(User worker) {
        if (worker.getRole() != Role.INDIVIDUAL) return;

        worker.setGradeScore(0.0);
        worker.setGrade(calculateGrade(0.0));
        userRepository.save(worker);

        log.info("[등급] 노쇼 점수 초기화 userId={}", worker.getId());
    }

    private String calculateGrade(double score) {
        if (score >= PROMOTER_MIN) return "프로모터";
        if (score >= PRO_MIN)      return "프로";
        return "스탭";
    }

    public String getGradeDescription(String grade) {
        return switch (grade) {
            case "프로"    -> "점수 3.1 이상";
            case "프로모터" -> "점수 5.0 이상";
            default        -> "스탭 (0.0 ~ 3.0)";
        };
    }
}
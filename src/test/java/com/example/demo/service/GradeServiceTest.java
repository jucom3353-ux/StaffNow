package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @InjectMocks
    private GradeService gradeService;

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;

    private User worker;
    private Application application;

    @BeforeEach
    void setUp() {
        worker = new User();
        worker.setId(1L);
        worker.setRole(Role.INDIVIDUAL);
        worker.setGrade("스탭");
        worker.setGradeScore(0.0);

        application = new Application();
        application.setUser(worker);
    }

    private Review makeReview(int rating) {
        Review review = new Review();
        review.setRating(rating);
        review.setReviewType(ReviewType.COMPANY_TO_WORKER);
        return review;
    }

    // ===== 초기 3건 혜택 테스트 =====

    @Test
    @DisplayName("초기 1건 - 별점 5점 → 0.1점 추가")
    void earlyBenefit_firstReview_rating5() {
        Review review = makeReview(5);
        given(reviewRepository.findByWorkerAndReviewType(worker, ReviewType.COMPANY_TO_WORKER))
                .willReturn(List.of());

        gradeService.applyReviewScore(worker, application, review, AttendanceStatus.NORMAL);

        // 5 × 0.02 = 0.1
        assertThat(worker.getGradeScore()).isEqualTo(0.1);
        assertThat(worker.getGrade()).isEqualTo("스탭");
        verify(userRepository, times(1)).save(worker);
    }

    @Test
    @DisplayName("초기 3건 완료 - 별점 5점씩 → 0.1씩 총 0.3 누적")
    void earlyBenefit_threeReviews_accumulated() {
        Review review = makeReview(5);
        given(reviewRepository.findByWorkerAndReviewType(worker, ReviewType.COMPANY_TO_WORKER))
                .willReturn(List.of(makeReview(5), makeReview(5)));

        worker.setGradeScore(0.2);

        gradeService.applyReviewScore(worker, application, review, AttendanceStatus.NORMAL);

        // 5 × 0.02 = 0.1 추가 → 0.3
        assertThat(worker.getGradeScore()).isEqualTo(0.3);
        assertThat(worker.getGrade()).isEqualTo("스탭");
    }

    // ===== 4건 이후 일반 점수 테스트 =====

    @Test
    @DisplayName("4건 이후 정상 출근 - 0.05 + 별점 × 0.01")
    void normalScore_afterEarlyBenefit() {
        Review review = makeReview(5);
        // 3건 이미 완료
        given(reviewRepository.findByWorkerAndReviewType(worker, ReviewType.COMPANY_TO_WORKER))
                .willReturn(List.of(makeReview(5), makeReview(5), makeReview(5)));

        worker.setGradeScore(1.5);

        gradeService.applyReviewScore(worker, application, review, AttendanceStatus.NORMAL);

        // 0.05 + (5 × 0.01) = 0.1 추가 → 1.6
        assertThat(worker.getGradeScore()).isEqualTo(1.6);
    }

    @Test
    @DisplayName("지각 시 기본 0.05 미적용 - 별점 점수만 추가")
    void lateAttendance_onlyStarScore() {
        Review review = makeReview(5);
        given(reviewRepository.findByWorkerAndReviewType(worker, ReviewType.COMPANY_TO_WORKER))
                .willReturn(List.of(makeReview(5), makeReview(5), makeReview(5)));

        worker.setGradeScore(1.5);

        gradeService.applyReviewScore(worker, application, review, AttendanceStatus.LATE);

        // 0.05 미적용, 5 × 0.01 = 0.05 추가 → 1.55
        assertThat(worker.getGradeScore()).isEqualTo(1.55);
    }

    // ===== 노쇼 테스트 =====

    @Test
    @DisplayName("노쇼 시 점수 0으로 초기화")
    void noShow_resetScore() {
        worker.setGradeScore(3.5);
        worker.setGrade("프로");

        gradeService.applyReviewScore(worker, application, makeReview(5), AttendanceStatus.ABSENT);

        assertThat(worker.getGradeScore()).isEqualTo(0.0);
        assertThat(worker.getGrade()).isEqualTo("스탭");
        verify(userRepository, times(1)).save(worker);
    }

    @Test
    @DisplayName("applyNoShow() - 점수 0 초기화")
    void applyNoShow_resetScore() {
        worker.setGradeScore(4.5);
        worker.setGrade("프로");

        gradeService.applyNoShow(worker);

        assertThat(worker.getGradeScore()).isEqualTo(0.0);
        assertThat(worker.getGrade()).isEqualTo("스탭");
        verify(userRepository, times(1)).save(worker);
    }

    // ===== 등급 전환 테스트 =====

    @Test
    @DisplayName("점수 3.1 이상 → 프로 등급")
    void gradePromotion_toProAtScore3_1() {
        Review review = makeReview(10);
        given(reviewRepository.findByWorkerAndReviewType(worker, ReviewType.COMPANY_TO_WORKER))
                .willReturn(List.of(makeReview(5), makeReview(5), makeReview(5)));

        worker.setGradeScore(3.0);

        gradeService.applyReviewScore(worker, application, review, AttendanceStatus.NORMAL);

        // 0.05 + 0.1 = 0.15 추가 → 3.15
        assertThat(worker.getGrade()).isEqualTo("프로");
    }

    @Test
    @DisplayName("점수 5.0 이상 → 프로모터 등급")
    void gradePromotion_toPromoterAtScore5() {
        Review review = makeReview(5);
        given(reviewRepository.findByWorkerAndReviewType(worker, ReviewType.COMPANY_TO_WORKER))
                .willReturn(List.of(makeReview(5), makeReview(5), makeReview(5)));

        worker.setGradeScore(4.9);
        worker.setGrade("프로");

        gradeService.applyReviewScore(worker, application, review, AttendanceStatus.NORMAL);

        // 0.05 + (5 × 0.01) = 0.1 추가 → 5.0
        assertThat(worker.getGradeScore()).isEqualTo(5.0);
        assertThat(worker.getGrade()).isEqualTo("프로모터");
    }

    // ===== 권한 검증 테스트 =====

    @Test
    @DisplayName("구직자 아닌 경우 등급 업데이트 안함")
    void applyReviewScore_notWorker() {
        User company = new User();
        company.setId(99L);
        company.setRole(Role.COMPANY);
        company.setGradeScore(0.0);

        Review review = makeReview(5);

        gradeService.applyReviewScore(company, application, review, AttendanceStatus.NORMAL);

        assertThat(company.getGradeScore()).isEqualTo(0.0);
        verify(userRepository, never()).save(any());
    }
}
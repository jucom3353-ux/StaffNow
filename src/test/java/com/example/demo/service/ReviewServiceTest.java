package com.example.demo.service;

import com.example.demo.dto.ReviewRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock private ReviewRepository reviewRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkAttendanceRepository workAttendanceRepository;
    @Mock private GradeService gradeService;

    private User company;
    private User worker;
    private User admin;
    private JobPost jobPost;
    private Application application;
    private ReviewRequestDto requestDto;

    @BeforeEach
    void setUp() {
        company = new User();
        company.setId(1L);
        company.setRole(Role.COMPANY);
        company.setName("롯데마트");

        worker = new User();
        worker.setId(2L);
        worker.setRole(Role.INDIVIDUAL);
        worker.setName("홍길동");
        worker.setTemperature(36.5);
        worker.setGradeScore(0.0);
        worker.setGrade("스탭");

        admin = new User();
        admin.setId(3L);
        admin.setRole(Role.ADMIN);

        jobPost = new JobPost();
        jobPost.setId(1L);
        jobPost.setTitle("프로모터 모집");
        jobPost.setUser(company);

        application = new Application();
        application.setUser(worker);
        application.setJobPost(jobPost);
        application.setStatus(ApplicationStatus.COMPLETED);

        requestDto = new ReviewRequestDto();
        requestDto.setRating(5);
        requestDto.setComment("성실하게 잘 해줬습니다.");
        requestDto.setSincerityRating(5);
        requestDto.setKindnessRating(4);
        requestDto.setSkillRating(5);
    }

    // ===== createReview() 테스트 =====

    @Test
    @DisplayName("기업 → 구직자 리뷰 작성 성공")
    void createReview_success() {
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(reviewRepository.existsByApplicationIdAndReviewType(
                1L, ReviewType.COMPANY_TO_WORKER)).willReturn(false);
        given(reviewRepository.save(any())).willReturn(new Review());
        given(workAttendanceRepository.findByApplicationId(1L))
                .willReturn(Optional.empty());
        doNothing().when(gradeService).applyReviewScore(any(), any(), any(), any());

        assertThatNoException().isThrownBy(() ->
                reviewService.createReview(1L, requestDto, company));

        verify(reviewRepository, times(1)).save(any());
        verify(gradeService, times(1)).applyReviewScore(any(), any(), any(), any());
    }

    @Test
    @DisplayName("구직자는 기업→구직자 리뷰 작성 불가")
    void createReview_fail_notCompany() {
        assertThatThrownBy(() ->
                reviewService.createReview(1L, requestDto, worker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("중복 리뷰 작성 불가")
    void createReview_fail_alreadyReviewed() {
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(reviewRepository.existsByApplicationIdAndReviewType(
                1L, ReviewType.COMPANY_TO_WORKER)).willReturn(true);

        assertThatThrownBy(() ->
                reviewService.createReview(1L, requestDto, company))
                .isInstanceOf(CustomException.class);
    }

    // ===== createWorkerReview() 테스트 =====

    @Test
    @DisplayName("구직자 → 기업 리뷰 작성 성공")
    void createWorkerReview_success() {
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));
        given(reviewRepository.existsByApplicationIdAndReviewType(
                1L, ReviewType.WORKER_TO_COMPANY)).willReturn(false);
        given(reviewRepository.save(any())).willReturn(new Review());

        assertThatNoException().isThrownBy(() ->
                reviewService.createWorkerReview(1L, requestDto, worker));

        verify(reviewRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("본인 지원 아니면 리뷰 작성 불가")
    void createWorkerReview_fail_notMyApplication() {
        User otherWorker = new User();
        otherWorker.setId(99L);
        otherWorker.setRole(Role.INDIVIDUAL);

        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));

        assertThatThrownBy(() ->
                reviewService.createWorkerReview(1L, requestDto, otherWorker))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("근무 완료 아닌 지원 리뷰 작성 불가")
    void createWorkerReview_fail_notCompleted() {
        application.setStatus(ApplicationStatus.APPROVED);
        given(applicationRepository.findById(1L)).willReturn(Optional.of(application));

        assertThatThrownBy(() ->
                reviewService.createWorkerReview(1L, requestDto, worker))
                .isInstanceOf(CustomException.class);
    }

    // ===== getWorkerRating() 테스트 =====

    @Test
    @DisplayName("구직자 평균 별점 조회 성공")
    void getWorkerRating_success() {
        Review review1 = new Review();
        review1.setRating(5);
        review1.setSincerityRating(5);
        review1.setKindnessRating(4);
        review1.setSkillRating(5);

        Review review2 = new Review();
        review2.setRating(3);
        review2.setSincerityRating(3);
        review2.setKindnessRating(3);
        review2.setSkillRating(3);

        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(reviewRepository.findByWorker(worker)).willReturn(List.of(review1, review2));

        var result = reviewService.getWorkerRating(2L);

        assertThat(result.getAverageRating()).isEqualTo(4.0);
        assertThat(result.getReviewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("리뷰 없는 구직자 별점 0.0")
    void getWorkerRating_noReviews() {
        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(reviewRepository.findByWorker(worker)).willReturn(List.of());

        var result = reviewService.getWorkerRating(2L);

        assertThat(result.getAverageRating()).isEqualTo(0.0);
        assertThat(result.getReviewCount()).isEqualTo(0);
    }

    // ===== deleteReview() 테스트 =====

    @Test
    @DisplayName("리뷰 삭제 성공 (관리자)")
    void deleteReview_success() {
        Review review = new Review();
        given(reviewRepository.findById(1L)).willReturn(Optional.of(review));

        reviewService.deleteReview(1L, admin);

        verify(reviewRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("관리자 아닌 경우 리뷰 삭제 불가")
    void deleteReview_fail_notAdmin() {
        assertThatThrownBy(() ->
                reviewService.deleteReview(1L, worker))
                .isInstanceOf(CustomException.class);
    }
}
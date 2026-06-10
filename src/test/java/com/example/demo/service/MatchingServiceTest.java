package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @InjectMocks
    private MatchingService matchingService;

    @Mock private JobPostRepository jobPostRepository;
    @Mock private UserRepository userRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private CompanySubscriptionRepository companySubscriptionRepository;
    @Mock private JobCategoryRepository jobCategoryRepository;
    @Mock private WorkAttendanceRepository workAttendanceRepository;
    @Mock private PreferredCategoryRepository preferredCategoryRepository;
    @Mock private ApplicationRepository applicationRepository;

    // 자동 매칭 - INDIVIDUAL이면 예외
    @Test
    void autoMatch_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> matchingService.autoMatch(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업");
    }

    // 자동 매칭 - 공고 없음
    @Test
    void autoMatch_jobPostNotFound_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        given(jobPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.autoMatch(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공고를 찾을 수 없습니다");
    }

    // 자동 매칭 - 본인 공고 아님
    @Test
    void autoMatch_notOwner_throwsException() {
        User owner = makeUser(1L, Role.COMPANY);
        User other = makeUser(2L, Role.COMPANY);

        JobPost post = new JobPost();
        post.setUser(owner);
        post.setPostStatus(PostStatus.OPEN);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> matchingService.autoMatch(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인 공고");
    }

    // 자동 매칭 - 마감된 공고
    @Test
    void autoMatch_closedJobPost_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        JobPost post = new JobPost();
        post.setUser(user);
        post.setPostStatus(PostStatus.CLOSED);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> matchingService.autoMatch(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("마감된 공고");
    }

    // 자동 매칭 - 구독 없음
    @Test
    void autoMatch_noSubscription_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        JobPost post = new JobPost();
        post.setUser(user);
        post.setPostStatus(PostStatus.OPEN);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(companySubscriptionRepository.findByCompanyAndStatus(user, SubscriptionStatus.ACTIVE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> matchingService.autoMatch(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구독 플랜이 필요합니다");
    }

    // 자동 매칭 - 카테고리 없음
    @Test
    void autoMatch_noCategory_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        JobPost post = new JobPost();
        post.setUser(user);
        post.setPostStatus(PostStatus.OPEN);
        post.setCategory(null);

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setMatchingLimit(10);

        CompanySubscription subscription = new CompanySubscription();
        subscription.setPlan(plan);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(companySubscriptionRepository.findByCompanyAndStatus(user, SubscriptionStatus.ACTIVE))
                .willReturn(Optional.of(subscription));

        assertThatThrownBy(() -> matchingService.autoMatch(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("카테고리가 설정되어 있지 않습니다");
    }

    // 구직자 공고 추천 - COMPANY이면 예외
    @Test
    void recommendJobPostsForWorker_companyUser_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> matchingService.recommendJobPostsForWorker(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구직자만");
    }

    // 구직자 공고 추천 - 선호 카테고리 없으면 전체 공고 반환
    @Test
    void recommendJobPostsForWorker_noPreferredCategory_returnsAll() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        given(preferredCategoryRepository.findCategoryIdsByUser(user))
                .willReturn(Collections.emptyList());
        given(jobPostRepository.findByPostStatus(PostStatus.OPEN))
                .willReturn(Collections.emptyList());

        List<?> result = matchingService.recommendJobPostsForWorker(user);

        then(jobPostRepository).should().findByPostStatus(PostStatus.OPEN);
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
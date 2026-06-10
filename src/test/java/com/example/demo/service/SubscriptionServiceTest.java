package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock private SubscriptionPlanRepository subscriptionPlanRepository;
    @Mock private CompanySubscriptionRepository companySubscriptionRepository;
    @Mock private ResumeViewHistoryRepository resumeViewHistoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private InvitationRepository invitationRepository;

    // 구독 조회 - INDIVIDUAL이면 예외
    @Test
    void getMySubscription_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> subscriptionService.getMySubscription(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업 회원만");
    }

    // 구독 - 플랜 없음
    @Test
    void subscribe_planNotFound_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        given(companySubscriptionRepository.findByCompanyAndStatus(user, SubscriptionStatus.ACTIVE))
                .willReturn(Optional.empty());
        given(subscriptionPlanRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.subscribe(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("플랜을 찾을 수 없습니다");
    }

    // 구독 취소 - 활성 구독 없음
    @Test
    void cancelSubscription_noActiveSubscription_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        given(companySubscriptionRepository.findByCompanyAndStatus(user, SubscriptionStatus.ACTIVE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.cancelSubscription(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("활성 구독이 없습니다");
    }

    // 구독 취소 - INDIVIDUAL이면 예외
    @Test
    void cancelSubscription_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> subscriptionService.cancelSubscription(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업 회원만");
    }

    // 이력서 조회 - INDIVIDUAL이면 예외
    @Test
    void viewResume_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> subscriptionService.viewResume(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업 회원만");
    }

    // 이력서 조회 - 이미 본 이력서면 true 반환
    @Test
    void viewResume_alreadyViewed_returnsTrue() {
        User company = makeUser(1L, Role.COMPANY);
        User worker = makeUser(2L, Role.INDIVIDUAL);

        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(resumeViewHistoryRepository.findByCompanyAndWorker(company, worker))
                .willReturn(Optional.of(new ResumeViewHistory()));

        boolean result = subscriptionService.viewResume(2L, company);

        assertThat(result).isTrue();
    }

    // 공고 등록 가능 여부 - INDIVIDUAL이면 예외
    @Test
    void canPostJob_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        assertThatThrownBy(() -> subscriptionService.canPostJob(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업 회원만");
    }

    // 공고 등록 가능 여부 - 구독 없고 1건 이상이면 false
    @Test
    void canPostJob_noSubscription_exceededFreeLimit_returnsFalse() {
        User user = makeUser(1L, Role.COMPANY);

        given(companySubscriptionRepository.findByCompanyAndStatus(user, SubscriptionStatus.ACTIVE))
                .willReturn(Optional.empty());
        given(jobPostRepository.countByUserAndPostStatusNot(user, PostStatus.CLOSED))
                .willReturn(1L);

        boolean result = subscriptionService.canPostJob(user);

        assertThat(result).isFalse();
    }

    // 초대 가능 여부 - 구독 없으면 false
    @Test
    void canInvite_noSubscription_returnsFalse() {
        User user = makeUser(1L, Role.COMPANY);

        given(companySubscriptionRepository.findByCompanyAndStatus(user, SubscriptionStatus.ACTIVE))
                .willReturn(Optional.empty());

        boolean result = subscriptionService.canInvite(user);

        assertThat(result).isFalse();
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
package com.example.demo.service;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class JobPostServiceTest {

    @InjectMocks
    private JobPostService jobPostService;

    @Mock private JobPostRepository jobPostRepository;
    @Mock private ApplicationRepository applicationRepository;
    @Mock private JobCategoryRepository jobCategoryRepository;
    @Mock private JobPostViewHistoryRepository jobPostViewHistoryRepository;
    @Mock private PreferredCategoryRepository preferredCategoryRepository;
    @Mock private SubscriptionService subscriptionService;
    @Mock private KakaoGeocodingService kakaoGeocodingService;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;

    // 공고 생성 - INDIVIDUAL이면 예외
    @Test
    void createJobPost_individualUser_throwsException() {
        User user = new User();
        user.setRole(Role.INDIVIDUAL);

        JobPostCreateRequestDto dto = new JobPostCreateRequestDto();

        assertThatThrownBy(() -> jobPostService.createJobPost(dto, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업");
    }

    // 공고 생성 - 구독 한도 초과
    @Test
    void createJobPost_subscriptionLimitExceeded_throwsException() {
        User user = new User();
        user.setRole(Role.COMPANY);

        JobPostCreateRequestDto dto = new JobPostCreateRequestDto();

        given(subscriptionService.canPostJob(user)).willReturn(false);

        assertThatThrownBy(() -> jobPostService.createJobPost(dto, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공고 등록 가능 횟수");
    }

    // 공고 조회 - 존재하지 않는 공고
    @Test
    void getJobPost_notFound_throwsException() {
        given(jobPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostService.getJobPost(999L, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공고를 찾을 수 없습니다");
    }

    // 공고 삭제 - 본인 공고 아님
    @Test
    void deleteJobPost_notOwner_throwsException() {
        User owner = new User();
        owner.setId(1L);
        owner.setRole(Role.COMPANY);

        User other = new User();
        other.setId(2L);
        other.setRole(Role.COMPANY);

        JobPost post = new JobPost();
        post.setUser(owner);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> jobPostService.deleteJobPost(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인 공고");
    }

    // 최근 본 공고 - COMPANY는 예외
    @Test
    void getRecentViews_companyUser_throwsException() {
        User user = new User();
        user.setRole(Role.COMPANY);

        assertThatThrownBy(() -> jobPostService.getRecentViews(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구직자만");
    }

    // 관리자 공고 조회 - ADMIN 아니면 예외
    @Test
    void adminGetAllJobPosts_notAdmin_throwsException() {
        User user = new User();
        user.setRole(Role.COMPANY);

        assertThatThrownBy(() -> jobPostService.adminGetAllJobPosts(null, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 공고 마감 처리 - ADMIN 아니면 예외
    @Test
    void adminCloseJobPost_notAdmin_throwsException() {
        User user = new User();
        user.setRole(Role.COMPANY);

        assertThatThrownBy(() -> jobPostService.adminCloseJobPost(1L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("관리자만");
    }

    // 공고 마감 처리 - 존재하지 않는 공고
    @Test
    void adminCloseJobPost_notFound_throwsException() {
        User user = new User();
        user.setRole(Role.ADMIN);

        given(jobPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostService.adminCloseJobPost(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공고를 찾을 수 없습니다");
    }
}
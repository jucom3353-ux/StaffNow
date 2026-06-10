package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.PreferredCategoryRepository;
import com.example.demo.repository.ProfileBoostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileBoostServiceTest {

    @InjectMocks
    private ProfileBoostService profileBoostService;

    @Mock private ProfileBoostRepository profileBoostRepository;
    @Mock private PreferredCategoryRepository preferredCategoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    // 부스트 시작 - COMPANY이면 예외
    @Test
    void startBoost_companyUser_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> profileBoostService.startBoost(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구직자만");
    }

    // 부스트 시작 - 이미 활성화
    @Test
    void startBoost_alreadyActive_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        given(profileBoostRepository.findActiveBoost(eq(user), any(LocalDateTime.class)))
                .willReturn(Optional.of(new ProfileBoost()));

        assertThatThrownBy(() -> profileBoostService.startBoost(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 활성화된 부스트");
    }

    // 부스트 취소 - COMPANY이면 예외
    @Test
    void cancelBoost_companyUser_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> profileBoostService.cancelBoost(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구직자만");
    }

    // 부스트 취소 - 활성 부스트 없음
    @Test
    void cancelBoost_notFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);

        given(profileBoostRepository.findActiveBoost(eq(user), any(LocalDateTime.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> profileBoostService.cancelBoost(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("활성화된 부스트를 찾을 수 없습니다");
    }

    // 내 부스트 조회 - COMPANY이면 예외
    @Test
    void getMyBoosts_companyUser_throwsException() {
        User user = makeUser(1L, Role.COMPANY);

        assertThatThrownBy(() -> profileBoostService.getMyBoosts(user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구직자만");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
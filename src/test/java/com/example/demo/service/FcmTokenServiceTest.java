package com.example.demo.service;

import com.example.demo.entity.FcmToken;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.FcmTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmTokenServiceTest {

    @InjectMocks
    private FcmTokenService fcmTokenService;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    // 토큰 등록 - 정상
    @Test
    void registerToken_success() {
        User user = makeUser(1L);
        String token = "test-fcm-token";

        given(fcmTokenRepository.findByUserAndToken(user, token))
                .willReturn(Optional.empty());

        fcmTokenService.registerToken(user, token, "Android");

        verify(fcmTokenRepository).save(any(FcmToken.class));
    }

    // 토큰 등록 - 이미 존재하면 스킵
    @Test
    void registerToken_alreadyExists_skip() {
        User user = makeUser(1L);
        String token = "test-fcm-token";

        given(fcmTokenRepository.findByUserAndToken(user, token))
                .willReturn(Optional.of(new FcmToken()));

        fcmTokenService.registerToken(user, token, "Android");

        verify(fcmTokenRepository, never()).save(any());
    }

    // 토큰 삭제 - 정상
    @Test
    void removeToken_success() {
        String token = "test-fcm-token";

        fcmTokenService.removeToken(token);

        verify(fcmTokenRepository).deleteByToken(token);
    }

    // 전체 토큰 삭제 - 정상
    @Test
    void removeAllTokens_success() {
        User user = makeUser(1L);

        fcmTokenService.removeAllTokens(user);

        verify(fcmTokenRepository).deleteAllByUser(user);
    }

    private User makeUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(Role.INDIVIDUAL);
        return user;
    }
}
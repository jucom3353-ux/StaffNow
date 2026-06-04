package com.example.demo.service;

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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock private NotificationRepository notificationRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private FcmTokenRepository fcmTokenRepository;
    @Mock private FcmService fcmService;

    private User worker;
    private Notification notification;

    @BeforeEach
    void setUp() {
        worker = new User();
        worker.setId(1L);
        worker.setRole(Role.INDIVIDUAL);
        worker.setName("홍길동");

        notification = new Notification();
        notification.setUser(worker);
        notification.setType(NotificationType.APPLICATION_APPROVED);
        notification.setMessage("지원이 승인되었습니다.");
        notification.setRead(false);
    }

    // ===== send() 테스트 =====

    @Test
    @DisplayName("알림 발송 성공 - DB 저장 + WebSocket 전송")
    void send_success() {
        given(notificationRepository.save(any())).willReturn(notification);
        given(fcmTokenRepository.findByUser(worker)).willReturn(List.of());

        notificationService.send(
                worker,
                NotificationType.APPLICATION_APPROVED,
                "지원이 승인되었습니다.",
                1L);

        verify(notificationRepository, times(1)).save(any());
        verify(messagingTemplate, times(1))
                .convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("FCM 토큰 있으면 푸시 알림 발송")
    void send_withFcmToken() {
        FcmToken fcmToken = new FcmToken();
        fcmToken.setUser(worker);
        fcmToken.setToken("test-fcm-token");

        given(notificationRepository.save(any())).willReturn(notification);
        given(fcmTokenRepository.findByUser(worker)).willReturn(List.of(fcmToken));

        notificationService.send(
                worker,
                NotificationType.APPLICATION_APPROVED,
                "지원이 승인되었습니다.",
                1L);

        verify(fcmService, times(1)).sendToTokens(any(), any(), any());
    }

    @Test
    @DisplayName("FCM 토큰 없으면 푸시 알림 미발송")
    void send_withoutFcmToken() {
        given(notificationRepository.save(any())).willReturn(notification);
        given(fcmTokenRepository.findByUser(worker)).willReturn(List.of());

        notificationService.send(
                worker,
                NotificationType.APPLICATION_APPROVED,
                "지원이 승인되었습니다.",
                1L);

        verify(fcmService, never()).sendToTokens(any(), any(), any());
    }

    // ===== markAsRead() 테스트 =====

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void markAsRead_success() {
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        notificationService.markAsRead(1L, worker);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    @DisplayName("본인 알림 아니면 읽음 처리 불가")
    void markAsRead_fail_notMyNotification() {
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setRole(Role.INDIVIDUAL);

        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

        assertThatThrownBy(() ->
                notificationService.markAsRead(1L, otherUser))
                .isInstanceOf(CustomException.class);
    }

    // ===== markAllAsRead() 테스트 =====

    @Test
    @DisplayName("전체 읽음 처리 성공")
    void markAllAsRead_success() {
        Notification n1 = new Notification();
        n1.setUser(worker);
        n1.setRead(false);

        Notification n2 = new Notification();
        n2.setUser(worker);
        n2.setRead(false);

        given(notificationRepository.findByUserAndIsReadFalse(worker))
                .willReturn(List.of(n1, n2));

        notificationService.markAllAsRead(worker);

        assertThat(n1.isRead()).isTrue();
        assertThat(n2.isRead()).isTrue();
        verify(notificationRepository, times(1)).saveAll(any());
    }

    // ===== getUnreadCount() 테스트 =====

    @Test
    @DisplayName("안 읽은 알림 수 조회")
    void getUnreadCount_success() {
        given(notificationRepository.countByUserAndIsReadFalse(worker)).willReturn(5);

        int count = notificationService.getUnreadCount(worker);

        assertThat(count).isEqualTo(5);
    }
}
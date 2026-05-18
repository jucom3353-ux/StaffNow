package com.example.demo.service;

import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 알림 생성 + 실시간 전송
    @Transactional
    public void send(User receiver, NotificationType type, String message, Long referenceId) {

        // DB 저장
        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);

        // WebSocket 실시간 전송
        messagingTemplate.convertAndSend(
                "/topic/notification." + receiver.getId(),
                new NotificationResponseDto(notification)
        );
    }

    // 내 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getMyNotifications(User loginUser) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    // 안 읽은 알림 목록
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotifications(User loginUser) {
        return notificationRepository.findByUserAndIsReadFalse(loginUser)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    // 안 읽은 알림 수
    @Transactional(readOnly = true)
    public int getUnreadCount(User loginUser) {
        return notificationRepository.countByUserAndIsReadFalse(loginUser);
    }

    // 알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId, User loginUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림 없음"));

        if (!notification.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 알림만 읽음 처리 가능");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // 전체 읽음 처리
    @Transactional
    public void markAllAsRead(User loginUser) {
        List<Notification> unread = notificationRepository
                .findByUserAndIsReadFalse(loginUser);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
package com.example.demo.service;

import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    @Transactional
    public void send(User receiver, NotificationType type, String message, Long referenceId) {

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);

        messagingTemplate.convertAndSend(
                "/topic/notification." + receiver.getId(),
                new NotificationResponseDto(notification)
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getMyNotifications(User loginUser) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotifications(User loginUser) {
        return notificationRepository.findByUserAndIsReadFalse(loginUser)
                .stream()
                .map(NotificationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(User loginUser) {
        return notificationRepository.countByUserAndIsReadFalse(loginUser);
    }

    @Transactional
    public void markAsRead(Long notificationId, User loginUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User loginUser) {
        List<Notification> unread = notificationRepository
                .findByUserAndIsReadFalse(loginUser);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
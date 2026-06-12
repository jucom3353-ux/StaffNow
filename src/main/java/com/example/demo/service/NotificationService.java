package com.example.demo.service;

import com.example.demo.dto.NotificationResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.FcmTokenRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmService fcmService;
    private final SmsService smsService;
    private final UserRepository userRepository;

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

        try {
            List<String> tokens = fcmTokenRepository.findByUser(receiver)
                    .stream()
                    .map(t -> t.getToken())
                    .collect(Collectors.toList());
            if (!tokens.isEmpty()) {
                fcmService.sendToTokens(tokens, getTitle(type), message);
            }
        } catch (Exception e) {
            log.error("FCM 발송 실패: userId={}, error={}", receiver.getId(), e.getMessage());
        }

        if (receiver.getPhone() != null && !receiver.getPhone().isBlank()) {
            try {
                smsService.send(receiver.getPhone(), message);
            } catch (Exception e) {
                log.error("SMS 발송 실패: userId={}, error={}", receiver.getId(), e.getMessage());
            }
        }
    }

    public void sendPopup(User receiver, NotificationType type, String message, Long referenceId) {
        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceId(referenceId);

        messagingTemplate.convertAndSend(
                "/topic/popup." + receiver.getId(),
                new NotificationResponseDto(notification)
        );
    }

    private String getTitle(NotificationType type) {
        return switch (type) {
            case APPLICATION_APPROVED -> "지원 승인";
            case APPLICATION_REJECTED -> "지원 거절";
            case APPLICATION_NO_SHOW -> "노쇼 처리";
            case APPLICATION_ABSENT -> "결근 처리";
            case CONTRACT_CREATED -> "계약서 생성";
            case CONTRACT_SIGNED -> "계약서 서명 완료";
            case CONTRACT_COMPLETED -> "계약 체결";
            case CONTRACT_CANCELLED -> "계약 취소";
            case PAYROLL_CREATED -> "정산 생성";
            case PAYROLL_AUTO_CONFIRMED -> "정산 자동 확정";
            case PAYROLL_CONFIRMED -> "정산 확정";
            case PAYROLL_PAID -> "정산 지급";
            case PAYROLL_REJECTED -> "정산 반려";
            case MESSAGE_RECEIVED -> "새 메시지";
            case INVITATION_RECEIVED -> "초대 수신";
            case DISPUTE_CREATED -> "분쟁 신청";
            case DISPUTE_ACCEPTED -> "분쟁 수락";
            case DISPUTE_DECLINED -> "분쟁 거절";
            case DISPUTE_RESOLVED -> "분쟁 중재 완료";
            case ATTENDANCE_CHECKED_IN -> "출근 완료";
            case ATTENDANCE_CHECKED_OUT -> "퇴근 완료";
            case ATTENDANCE_LATE -> "지각 처리";
            case ATTENDANCE_PHOTO -> "출퇴근 사진";
            case REPORT_APPROVED -> "신고 승인";
            case REPORT_DISMISSED -> "신고 기각";
            case SUBSCRIPTION_EXPIRING_SOON -> "구독 만료 임박";
            case SUBSCRIPTION_EXPIRED -> "구독 만료";
            case SUBSCRIPTION_RENEWED -> "구독 갱신";
            case SUBSCRIPTION_RENEWAL_FAILED -> "구독 갱신 실패";
            case JOB_POST_CLOSED -> "공고 마감";
            case NEW_JOB_POST -> "새 공고";
            case LATE_APPEAL_RECEIVED -> "소명 접수";
            case LATE_APPEAL_APPROVED -> "소명 승인";
            case LATE_APPEAL_REJECTED -> "소명 반려";
            case ATTENDANCE_DISPUTE_RECEIVED -> "출퇴근 분쟁 접수";
            case ATTENDANCE_DISPUTE_APPROVED -> "출퇴근 분쟁 승인";
            case ATTENDANCE_DISPUTE_REJECTED -> "출퇴근 분쟁 반려";
            case INQUIRY_REPLIED -> "문의 답변";
            case NOTICE_RECEIVED -> "공지사항";
            case URGENT_JOB_POST -> "긴급 공고";
            case BOOKMARK_DEADLINE_SOON -> "마감 임박";
            case BOOKMARK_JOB_CLOSED -> "북마크 공고 마감";
            case ACCOUNT_SUSPENDED -> "계정 이용제한";
            case ACCOUNT_WARNING -> "계정 주의";
            case ADMIN_ALERT -> "관리자 알림";
            case GOAL_ACHIEVED -> "목표 달성";
            case BOOST_ACTIVATED -> "부스트 알림";
            case ADMIN_MESSAGE -> "관리자 공지";
        };
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getMyNotifications(User loginUser) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream().map(NotificationResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotifications(User loginUser) {
        return notificationRepository.findByUserAndIsReadFalse(loginUser)
                .stream().map(NotificationResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(User loginUser) {
        return notificationRepository.countByUserAndIsReadFalse(loginUser);
    }

    @Transactional
    public void markAsRead(Long notificationId, User loginUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUser().getId().equals(loginUser.getId()))
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User loginUser) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(loginUser);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Transactional
    public void deleteNotification(Long notificationId, User loginUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.getUser().getId().equals(loginUser.getId()))
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteSelected(List<Long> ids, User loginUser) {
        List<Notification> notifications = notificationRepository.findAllById(ids);
        notifications.forEach(n -> {
            if (!n.getUser().getId().equals(loginUser.getId()))
                throw new CustomException(ErrorCode.ACCESS_DENIED);
        });
        notificationRepository.deleteAll(notifications);
    }

    @Transactional
    public void deleteAll(User loginUser) {
        notificationRepository.deleteByUser(loginUser);
    }

    @Transactional
    public void broadcast(String title, String body, String target, List<Long> userIds, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN)
            throw new CustomException(ErrorCode.ADMIN_ONLY);

        List<User> targets;
        if ("specific".equals(target) && userIds != null) {
            targets = userRepository.findAllById(userIds);
        } else if ("individual".equals(target)) {
            targets = userRepository.findByRole(Role.INDIVIDUAL);
        } else if ("company".equals(target)) {
            targets = userRepository.findByRole(Role.COMPANY);
        } else {
            targets = userRepository.findAll();
        }

        targets.forEach(user ->
                send(user, NotificationType.ADMIN_MESSAGE, title + "\n" + body, null));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getAdminMessageHistory(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN)
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        return notificationRepository.findByTypeOrderByCreatedAtDesc(NotificationType.ADMIN_MESSAGE)
                .stream().map(NotificationResponseDto::new).collect(Collectors.toList());
    }
}
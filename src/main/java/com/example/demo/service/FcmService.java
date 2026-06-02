package com.example.demo.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    // 단일 기기 발송
    public void sendToToken(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 발송 완료: {}", response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 실패: token={}, error={}", token, e.getMessage());
        }
    }

    // 다중 기기 발송 (멀티 기기 대응)
    public void sendToTokens(List<String> tokens, String title, String body) {
        if (tokens == null || tokens.isEmpty()) return;

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance()
                    .sendEachForMulticast(message);

            log.info("FCM 멀티 발송 완료: 성공={}, 실패={}",
                    response.getSuccessCount(),
                    response.getFailureCount());

        } catch (FirebaseMessagingException e) {
            log.error("FCM 멀티 발송 실패: error={}", e.getMessage());
        }
    }
}
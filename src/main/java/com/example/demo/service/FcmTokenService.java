package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.FcmToken;
import com.example.demo.entity.User;
import com.example.demo.repository.FcmTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    // 토큰 등록 (이미 있으면 스킵)
    @Transactional
    public void registerToken(User user, String token, String deviceInfo) {
        // 동일 토큰 이미 있으면 스킵
        if (fcmTokenRepository.findByUserAndToken(user, token).isPresent()) {
            return;
        }

        FcmToken fcmToken = new FcmToken();
        fcmToken.setUser(user);
        fcmToken.setToken(token);
        fcmToken.setDeviceInfo(deviceInfo);
        fcmTokenRepository.save(fcmToken);
    }

    // 토큰 삭제 (로그아웃 시)
    @Transactional
    public void removeToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }

    // 유저의 모든 토큰 삭제 (회원 탈퇴 시)
    @Transactional
    public void removeAllTokens(User user) {
        fcmTokenRepository.deleteAllByUser(user);
    }

    // 유저의 모든 FCM 토큰 목록 조회 (푸시 발송 시 사용)
    @Transactional(readOnly = true)
    public List<String> getTokens(User user) {
        return fcmTokenRepository.findByUser(user)
                .stream()
                .map(FcmToken::getToken)
                .collect(Collectors.toList());
    }
}
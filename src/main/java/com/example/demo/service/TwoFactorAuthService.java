package com.example.demo.service;

import com.example.demo.entity.TwoFactorAuth;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.TwoFactorAuthRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    private static final int CODE_EXPIRE_MINUTES = 5;

    // 2단계 인증 활성화/비활성화
    @Transactional
    public void toggleTwoFactor(User loginUser) {
        loginUser.setTwoFactorEnabled(!loginUser.isTwoFactorEnabled());
        userRepository.save(loginUser);
        log.info("2단계 인증 {}:  userId={}",
                loginUser.isTwoFactorEnabled() ? "활성화" : "비활성화",
                loginUser.getId());
    }

    // 인증 코드 발송
    @Transactional
    public void sendCode(User loginUser) {

        // 기존 코드 삭제
        twoFactorAuthRepository.deleteByUser(loginUser);

        String code = generateCode();

        TwoFactorAuth auth = new TwoFactorAuth();
        auth.setUser(loginUser);
        auth.setCode(code);
        auth.setExpiredAt(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        twoFactorAuthRepository.save(auth);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(loginUser.getEmail());
        message.setSubject("[StaffNow] 2단계 인증 코드");
        message.setText("인증 코드: " + code + "\n\n" +
                CODE_EXPIRE_MINUTES + "분간 유효합니다.\n" +
                "본인이 요청하지 않은 경우 이 메일을 무시하세요.");

        mailSender.send(message);
        log.info("2단계 인증 코드 발송: userId={}", loginUser.getId());
    }

    // 인증 코드 검증
    @Transactional
    public void verifyCode(User loginUser, String code) {

        TwoFactorAuth auth = twoFactorAuthRepository
                .findTopByUserOrderByCreatedAtDesc(loginUser)
                .orElseThrow(() -> new CustomException(ErrorCode.VERIFY_CODE_REQUIRED));

        if (auth.getExpiredAt().isBefore(LocalDateTime.now())) {
            twoFactorAuthRepository.deleteByUser(loginUser);
            throw new CustomException(ErrorCode.VERIFY_CODE_EXPIRED);
        }

        if (!auth.getCode().equals(code)) {
            throw new CustomException(ErrorCode.VERIFY_CODE_INVALID);
        }

        auth.setVerified(true);
        twoFactorAuthRepository.save(auth);

        log.info("2단계 인증 완료: userId={}", loginUser.getId());
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
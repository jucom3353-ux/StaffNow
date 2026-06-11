package com.example.demo.service;

import com.example.demo.entity.TwoFactorAuth;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.TwoFactorAuthRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {

    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_ATTEMPT = 5;
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final int LOCK_MINUTES = 30;

    @Transactional
    public void sendCode(User user) {
        twoFactorAuthRepository.deleteByUser(user);

        String rawCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        TwoFactorAuth otp = new TwoFactorAuth();
        otp.setUser(user);
        otp.setCode(passwordEncoder.encode(rawCode));
        otp.setExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));
        twoFactorAuthRepository.save(otp);

        sendOtpEmail(user.getEmail(), rawCode);
    }

    @Transactional
    public void verifyCode(User user, String inputCode) {
        TwoFactorAuth otp = twoFactorAuthRepository
                .findTopByUserAndVerifiedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new CustomException(ErrorCode.OTP_NOT_FOUND));

        // 잠금 체크
        if (otp.getLockedUntil() != null
                && LocalDateTime.now().isBefore(otp.getLockedUntil())) {
            throw new CustomException(ErrorCode.OTP_LOCKED);
        }

        // 만료 체크
        if (LocalDateTime.now().isAfter(otp.getExpiredAt())) {
            twoFactorAuthRepository.delete(otp);
            throw new CustomException(ErrorCode.OTP_EXPIRED);
        }

        // 코드 불일치
        if (!passwordEncoder.matches(inputCode, otp.getCode())) {
            int attempts = otp.getAttemptCount() + 1;
            otp.setAttemptCount(attempts);
            if (attempts >= MAX_ATTEMPT) {
                otp.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }
            twoFactorAuthRepository.save(otp);
            throw new CustomException(ErrorCode.OTP_INVALID);
        }

        otp.setVerified(true);
        twoFactorAuthRepository.save(otp);
    }

    @Transactional
    public void toggleTwoFactor(User user) {
        user.setTwoFactorEnabled(!user.isTwoFactorEnabled());
        userRepository.save(user);
    }

    @Async
    public void sendOtpEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[Promoter] 관리자 인증 코드");
        message.setText(
            "인증 코드: " + code + "\n\n" +
            "유효 시간: 5분\n" +
            "타인에게 절대 공유하지 마세요."
        );
        mailSender.send(message);
    }
}
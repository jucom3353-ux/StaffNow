package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // 인증코드 임시 저장 (이메일 → {code, expiredAt})
    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();

    private static final int CODE_EXPIRE_MINUTES = 5; // 5분 유효

    // 1. 인증코드 발송
    @Transactional(readOnly = true)
    public void sendResetCode(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        String code = generateCode();
        codeStore.put(email, new CodeInfo(code,
                LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES)));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[StaffNow] 비밀번호 재설정 인증코드");
        message.setText("인증코드: " + code + "\n\n" +
                "해당 코드는 " + CODE_EXPIRE_MINUTES + "분간 유효합니다.\n" +
                "본인이 요청하지 않은 경우 이 메일을 무시하세요.");

        mailSender.send(message);
        log.info("비밀번호 재설정 인증코드 발송: email={}", email);
    }

    // 2. 인증코드 확인
    public void verifyCode(String email, String code) {
        CodeInfo info = codeStore.get(email);

        if (info == null) {
            throw new RuntimeException("인증코드를 먼저 요청해주세요.");
        }
        if (info.expiredAt().isBefore(LocalDateTime.now())) {
            codeStore.remove(email);
            throw new RuntimeException("인증코드가 만료되었습니다.");
        }
        if (!info.code().equals(code)) {
            throw new RuntimeException("인증코드가 일치하지 않습니다.");
        }
    }

    // 3. 비밀번호 변경
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        verifyCode(email, code);

        if (newPassword.length() < 8) {
            throw new RuntimeException("비밀번호는 8자 이상이어야 합니다.");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 사용된 코드 제거
        codeStore.remove(email);
        log.info("비밀번호 재설정 완료: email={}", email);
    }

    // 6자리 숫자 코드 생성
    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // 인증코드 저장 레코드
    private record CodeInfo(String code, LocalDateTime expiredAt) {}
}
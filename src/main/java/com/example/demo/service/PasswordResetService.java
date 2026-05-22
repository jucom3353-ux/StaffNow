package com.example.demo.service;

import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();
    private static final int CODE_EXPIRE_MINUTES = 5;

    @Transactional(readOnly = true)
    public void sendResetCode(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,
                        "가입되지 않은 이메일입니다."));

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

    public void verifyCode(String email, String code) {
        CodeInfo info = codeStore.get(email);

        if (info == null) {
            throw new CustomException(ErrorCode.VERIFY_CODE_REQUIRED);
        }
        if (info.expiredAt().isBefore(LocalDateTime.now())) {
            codeStore.remove(email);
            throw new CustomException(ErrorCode.VERIFY_CODE_EXPIRED);
        }
        if (!info.code().equals(code)) {
            throw new CustomException(ErrorCode.VERIFY_CODE_INVALID);
        }
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        verifyCode(email, code);

        if (newPassword.length() < 8) {
            throw new CustomException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        codeStore.remove(email);
        log.info("비밀번호 재설정 완료: email={}", email);
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private record CodeInfo(String code, LocalDateTime expiredAt) {}
}
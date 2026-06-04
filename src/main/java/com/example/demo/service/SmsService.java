package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    @Value("${coolsms.api-key:}")
    private String apiKey;

    @Value("${coolsms.api-secret:}")
    private String apiSecret;

    @Value("${coolsms.sender:}")
    private String sender;

    private static final boolean SMS_ENABLED = false; // 연동 후 true로 변경

    public void send(String to, String message) {
        if (!SMS_ENABLED || apiKey.isBlank()) {
            log.info("[SMS 미전송 - 미연동] to={}, message={}", to, message);
            return;
        }

        try {
            // CoolSMS 연동 후 아래 주석 해제
            // DefaultMessageService messageService =
            //         NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
            // Message sms = new Message();
            // sms.setFrom(sender);
            // sms.setTo(to);
            // sms.setText(message);
            // messageService.sendOne(new SingleMessageSendingRequest(sms));
            log.info("[SMS 발송] to={}, message={}", to, message);
        } catch (Exception e) {
            log.error("[SMS 발송 실패] to={}, error={}", to, e.getMessage());
        }
    }

    // 인증코드 발송
    public void sendVerificationCode(String to, String code) {
        send(to, "[StaffNow] 인증번호: " + code + " (5분 내 입력)");
    }

    // 공고 마감 D-1 알림
    public void sendJobPostDeadlineAlert(String to, String jobTitle) {
        send(to, "[StaffNow] '" + jobTitle + "' 공고가 내일 마감됩니다.");
    }

    // 지원 승인 알림
    public void sendApplicationApproved(String to, String jobTitle) {
        send(to, "[StaffNow] '" + jobTitle + "' 지원이 승인되었습니다.");
    }

    // 급여 지급 알림
    public void sendPayrollPaid(String to, String jobTitle, int amount) {
        send(to, "[StaffNow] '" + jobTitle + "' 급여 " + amount + "원이 지급되었습니다.");
    }

    // 계약서 서명 요청
    public void sendContractSignRequest(String to, String jobTitle) {
        send(to, "[StaffNow] '" + jobTitle + "' 근로계약서 서명을 요청합니다.");
    }
}
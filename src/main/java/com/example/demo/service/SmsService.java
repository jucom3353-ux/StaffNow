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
        send(to, "[Promoter] 인증번호: " + code + " (5분 내 입력)");
    }

    // 공고 마감 D-1 알림
    public void sendJobPostDeadlineAlert(String to, String jobTitle) {
        send(to, "[Promoter] '" + jobTitle + "' 공고가 내일 마감됩니다.");
    }

    // 지원 승인 알림
    public void sendApplicationApproved(String to, String jobTitle,
        String workStartDate, String workLocation) {
        send(to, "[Promoter] '" + jobTitle + "' 지원이 승인되었습니다.\n"
            + "▶ 근무 시작일: " + workStartDate + "\n"
            + "▶ 근무 장소: " + workLocation + "\n"
            + "Promoter 앱에서 계약서 서명을 완료해 주세요.");
    }

    // 공고 미채용
    public void sendApplicationRejected(String to, String jobTitle) {
        send(to, "[Promoter] '" + jobTitle + "' 공고 지원 건이 미채용으로 결정되었습니다.\n"
            + "다른 공고를 확인해 보세요.");
    }

    // 급여 지급 알림
    public void sendPayrollPaid(String to, String jobTitle,
        String weekStart, int totalPay, int netPay) {
        send(to, "[Promoter] '" + jobTitle + "' " + weekStart + " 주차 급여가 지급되었습니다.\n"
            + "▶ 지급액: " + totalPay + "원\n"
            + "▶ 실수령액: " + netPay + "원");
    }

    // 계약서 서명 요청
    public void sendContractSignRequest(String to, String jobTitle, String deadline) {
        send(to, "[Promoter] '" + jobTitle + "' 근로계약서 서명을 요청드립니다.\n"
            + "▶ 서명 기한: " + deadline + "\n"
            + "Promoter 앱에서 계약서를 확인하고 서명해 주세요.");
    }

    // 계약서 서명 완료
    public void sendContractSigned(String to, String workerName,
        String jobTitle, String signedAt) {
        send(to, "[Promoter] " + workerName + "님이 '" + jobTitle
            + "' 근로계약서 서명을 완료했습니다.\n"
            + "▶ 서명 일시: " + signedAt + "\n"
            + "Promoter 앱에서 계약서를 확인해 주세요.");
    }

    // 지원자 확인
    public void sendNewApplicant(String to, String jobTitle,
        String applicantName, String appliedAt) {
        send(to, "[Promoter] '" + jobTitle + "' 공고에 새로운 지원자가 있습니다.\n"
            + "▶ 지원자: " + applicantName + "\n"
            + "▶ 지원 일시: " + appliedAt + "\n"
            + "Promoter 앱에서 지원자를 확인해 주세요.");
    }

}
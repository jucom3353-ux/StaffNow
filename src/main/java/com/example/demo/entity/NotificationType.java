package com.example.demo.entity;

public enum NotificationType {
    // 지원 관련
    APPLICATION_APPROVED,
    APPLICATION_REJECTED,

    // 초대 관련
    INVITATION_RECEIVED,

    // 계약 관련
    CONTRACT_CREATED,           // 계약서 생성
    CONTRACT_SIGNED,            // 상대방 서명 완료
    CONTRACT_COMPLETED,         // 양쪽 모두 서명 → 계약 체결
    CONTRACT_CANCELLED,         // 계약 취소

    // 메시지 관련
    MESSAGE_RECEIVED,

    // 정산 관련
    PAYROLL_CREATED,
    PAYROLL_CONFIRMED,
    PAYROLL_PAID,
    PAYROLL_REJECTED,
    PAYROLL_AUTO_CONFIRMED,

    // 분쟁 관련
    DISPUTE_CREATED,
    DISPUTE_ACCEPTED,
    DISPUTE_DECLINED,
    DISPUTE_RESOLVED,

    // 출퇴근 관련
    ATTENDANCE_CHECKED_IN,
    ATTENDANCE_CHECKED_OUT,
    ATTENDANCE_LATE,

    // 신고 관련
    REPORT_APPROVED,
    REPORT_DISMISSED,

    // 노쇼/결근 관련
    APPLICATION_NO_SHOW,
    APPLICATION_ABSENT,

    // 구독 관련
    SUBSCRIPTION_EXPIRING_SOON,
    SUBSCRIPTION_EXPIRED,
    SUBSCRIPTION_RENEWED,
    SUBSCRIPTION_RENEWAL_FAILED,

    // 공고 관련
    JOB_POST_CLOSED,

    LATE_APPEAL_RECEIVED,  // 소명 접수
    LATE_APPEAL_APPROVED,  // 소명 승인
    LATE_APPEAL_REJECTED,   // 소명 반려

    ATTENDANCE_DISPUTE_RECEIVED,  // 분쟁 접수
    ATTENDANCE_DISPUTE_APPROVED,  // 분쟁 승인
    ATTENDANCE_DISPUTE_REJECTED,   // 분쟁 반려
    
    INQUIRY_REPLIED, // 문의 답변
}
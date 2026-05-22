package com.example.demo.entity;

public enum NotificationType {
    // 지원 관련
    APPLICATION_APPROVED,       // 지원 승인
    APPLICATION_REJECTED,       // 지원 거절

    // 초대 관련
    INVITATION_RECEIVED,        // 초대 받음

    // 계약 관련
    CONTRACT_CREATED,           // 계약서 생성

    // 메시지 관련
    MESSAGE_RECEIVED,           // 메시지 받음

    // 정산 관련
    PAYROLL_CREATED,            // 정산 생성
    PAYROLL_CONFIRMED,          // 정산 확정
    PAYROLL_PAID,               // 급여 지급 완료
    PAYROLL_REJECTED,           // 정산 반려
    PAYROLL_AUTO_CONFIRMED,     // 2주 초과 자동 확정

    // 분쟁 관련
    DISPUTE_CREATED,            // 분쟁 신청
    DISPUTE_ACCEPTED,           // 분쟁 수락
    DISPUTE_DECLINED,           // 분쟁 거절
    DISPUTE_RESOLVED,           // 분쟁 중재 완료

    // 출퇴근 관련
    ATTENDANCE_CHECKED_IN,      // 출근 처리
    ATTENDANCE_CHECKED_OUT,     // 퇴근 처리
    ATTENDANCE_LATE,            // 지각 처리

    // 신고 관련
    REPORT_APPROVED,            // 신고 승인 (경고)
    REPORT_DISMISSED,            // 신고 기각

    // NotificationType.java에 추가
    APPLICATION_NO_SHOW,    // 노쇼 처리
    APPLICATION_ABSENT,     // 결근 처리

    // 구독 관련
    SUBSCRIPTION_EXPIRING_SOON,     // 만료 7일 전 알림
    SUBSCRIPTION_EXPIRED,           // 만료 알림
    SUBSCRIPTION_RENEWED,           // 자동 갱신 완료
    SUBSCRIPTION_RENEWAL_FAILED,    // 자동 갱신 실패
}
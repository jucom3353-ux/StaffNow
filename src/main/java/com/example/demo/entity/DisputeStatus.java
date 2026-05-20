package com.example.demo.entity;

public enum DisputeStatus {
    PENDING,    // 기업이 분쟁 신청, 근로자 응답 대기
    ACCEPTED,   // 근로자 수락 → 정산 CONFIRMED 처리
    DECLINED,   // 근로자 거절 → ADMIN 개입 필요
    RESOLVED,   // ADMIN 중재 완료
    CLOSED      // 최종 종료
}
package com.example.demo.entity;

public enum PayrollStatus {
    PENDING,    // 정산 대기
    CONFIRMED,  // 기업 확정
    PAID,       // 지급 완료
    REJECTED    // 반려
}
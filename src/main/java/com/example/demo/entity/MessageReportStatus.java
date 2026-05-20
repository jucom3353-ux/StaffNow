package com.example.demo.entity;

public enum MessageReportStatus {
    PENDING,    // 처리 대기
    APPROVED,   // 승인 (경고 처리)
    REVIEWED,   // 검토 완료
    DISMISSED   // 기각
}
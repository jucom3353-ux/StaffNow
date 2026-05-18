package com.example.demo.entity;

public enum ContractStatus {
    PENDING,    // 서명 대기
    SIGNED,     // 서명 완료
    CANCELLED,  // 취소
    EXPIRED     // 만료 (1개월 내 미서명)
}
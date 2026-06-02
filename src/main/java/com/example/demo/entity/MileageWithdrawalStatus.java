package com.example.demo.entity;
import com.example.demo.entity.MileageWithdrawalStatus;

public enum MileageWithdrawalStatus {
    PENDING,    // 승인 대기
    APPROVED,   // 승인 완료
    REJECTED,   // 거절
    CANCELLED   // 취소
}
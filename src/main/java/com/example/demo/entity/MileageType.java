package com.example.demo.entity;

public enum MileageType {
    // 적립
    CHECKIN_ON_TIME,        // 정시 출근
    WORK_COMPLETED,         // 근무 완료
    STREAK_BONUS,           // 노쇼 없이 10회 완료 보너스
    REVIEW_WRITTEN,         // 리뷰 작성
    REFERRAL_BONUS,         // 추천 코드 친구 가입

    // 차감
    NO_SHOW,                // 노쇼
    LATE,                   // 지각

    // 사용
    BOOST_USED,             // 부스트 1일권 교환

    // 출금
    WITHDRAWAL_REQUESTED,   // 출금 신청
    WITHDRAWAL_CANCELLED    // 출금 취소
}
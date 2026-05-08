package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 조회할 때 응답용 DTO
@Getter
@AllArgsConstructor
public class WorkSessionResponseDto {

    // 근무 날짜
    private String workDate;

    // 오전 / 오후 / 야간
    private String shift;

    // 연결된 공고 제목
    private String jobPostTitle;
}
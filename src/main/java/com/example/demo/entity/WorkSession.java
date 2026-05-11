package com.example.demo.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WorkSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 근무 날짜
    private String workDate;

    // 시작 시간
    private String startTime;

    // 종료 시간
    private String endTime;

    // 모집 인원
    private int recruitCount;

    // 현재 지원 인원
    private int currentCount;

    // 시급
    private int pay;

    // 모집 상태
    @Enumerated(EnumType.STRING)
    private WorkStatus status;

    // 공고 연결
    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;
}
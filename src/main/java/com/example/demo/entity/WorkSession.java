package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    // 급여
    private int pay;

    // 메모 (특이사항, 전달사항)
    private String memo;

    // 모집 상태
    @Enumerated(EnumType.STRING)
    private WorkStatus status;

    // 공고 연결
    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    // 출퇴근 직접 연결
    @OneToMany(mappedBy = "workSession", cascade = CascadeType.ALL)
    private List<WorkAttendance> attendances;
}
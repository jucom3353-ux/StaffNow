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

    private String shift;        // AM / PM / FULL

    private String workDate;
    private String startTime;
    private String endTime;

    private int recruitCount;
    private int currentCount;
    private int pay;
    private String memo;

    @Enumerated(EnumType.STRING)
    private WorkStatus status;

    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    @OneToMany(mappedBy = "workSession", cascade = CascadeType.ALL)
    private List<WorkAttendance> attendances;
}
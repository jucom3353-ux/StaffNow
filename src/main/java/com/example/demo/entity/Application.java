package com.example.demo.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 지원 유저
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 지원한 근무회차
    @ManyToOne
    @JoinColumn(name = "work_session_id")
    private WorkSession workSession;

    // 지원 상태
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;
}
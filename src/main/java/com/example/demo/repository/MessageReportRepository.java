package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Message;
import com.example.demo.entity.MessageReport;
import com.example.demo.entity.MessageReportStatus;
import com.example.demo.entity.User;


public interface MessageReportRepository extends JpaRepository<MessageReport, Long> {

    boolean existsByReporterAndMessage(User reporter, Message message);

    List<MessageReport> findByReporter(User reporter);

    // 추가: ADMIN 전용 상태별 조회
    List<MessageReport> findByStatus(MessageReportStatus status);
}
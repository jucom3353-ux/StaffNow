package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.WorkSession;
import com.example.demo.entity.WorkSessionQr;


public interface WorkSessionQrRepository extends JpaRepository<WorkSessionQr, Long> {
    Optional<WorkSessionQr> findByWorkSession(WorkSession workSession);
    Optional<WorkSessionQr> findByQrToken(String qrToken);
}
package com.example.demo.repository;

import com.example.demo.entity.WorkSession;
import com.example.demo.entity.WorkSessionQr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkSessionQrRepository extends JpaRepository<WorkSessionQr, Long> {
    Optional<WorkSessionQr> findByWorkSession(WorkSession workSession);
    Optional<WorkSessionQr> findByQrToken(String qrToken);
}
package com.example.demo.repository;

import com.example.demo.entity.Application;
import com.example.demo.entity.WorkAttendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkAttendanceRepository
        extends JpaRepository<WorkAttendance, Long> {

    Optional<WorkAttendance>
    findByApplication(Application application);
}
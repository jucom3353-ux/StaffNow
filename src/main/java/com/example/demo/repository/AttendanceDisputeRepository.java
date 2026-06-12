package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AttendanceDispute;
import com.example.demo.entity.AttendanceDisputeStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkAttendance;


public interface AttendanceDisputeRepository
        extends JpaRepository<AttendanceDispute, Long> {

    List<AttendanceDispute> findByWorker(User worker);
    List<AttendanceDispute> findByCompany(User company);
    List<AttendanceDispute> findByStatus(AttendanceDisputeStatus status);
    boolean existsByAttendanceAndWorker(WorkAttendance attendance, User worker);
}
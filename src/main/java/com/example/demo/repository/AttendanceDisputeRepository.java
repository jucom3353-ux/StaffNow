package com.example.demo.repository;

import com.example.demo.entity.AttendanceDispute;
import com.example.demo.entity.AttendanceDisputeStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceDisputeRepository
        extends JpaRepository<AttendanceDispute, Long> {

    List<AttendanceDispute> findByWorker(User worker);
    List<AttendanceDispute> findByCompany(User company);
    List<AttendanceDispute> findByStatus(AttendanceDisputeStatus status);
    boolean existsByAttendanceAndWorker(WorkAttendance attendance, User worker);
}
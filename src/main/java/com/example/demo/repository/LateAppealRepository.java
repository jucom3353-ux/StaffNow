package com.example.demo.repository;

import com.example.demo.entity.LateAppeal;
import com.example.demo.entity.LateAppealStatus;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.entity.WorkAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LateAppealRepository extends JpaRepository<LateAppeal, Long> {

    List<LateAppeal> findByWorker(User worker);
    List<LateAppeal> findByStatus(LateAppealStatus status);
    Optional<LateAppeal> findByAttendance(WorkAttendance attendance);
    boolean existsByAttendance(WorkAttendance attendance);
}
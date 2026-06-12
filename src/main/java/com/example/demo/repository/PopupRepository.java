package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Popup;


public interface PopupRepository extends JpaRepository<Popup, Long> {

    @Query("SELECT p FROM Popup p WHERE p.isActive = true " +
           "AND (p.startAt IS NULL OR p.startAt <= :now) " +
           "AND (p.endAt IS NULL OR p.endAt >= :now)")
    List<Popup> findActivePopups(@Param("now") LocalDateTime now);
}
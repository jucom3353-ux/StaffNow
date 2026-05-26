package com.example.demo.repository;

import com.example.demo.entity.Popup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

    @Query("SELECT p FROM Popup p WHERE p.isActive = true " +
           "AND (p.startAt IS NULL OR p.startAt <= :now) " +
           "AND (p.endAt IS NULL OR p.endAt >= :now)")
    List<Popup> findActivePopups(@Param("now") LocalDateTime now);
}
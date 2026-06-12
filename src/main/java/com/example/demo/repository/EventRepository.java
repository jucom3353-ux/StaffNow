package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Event;
import com.example.demo.entity.EventStatus;


public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByCreatedAtDesc(EventStatus status);
    List<Event> findAllByOrderByCreatedAtDesc();

    // ONGOING 중 종료일 지난 이벤트 (Scheduler용)
@Query("SELECT e FROM Event e WHERE e.status = 'ONGOING' " +
       "AND e.endDate IS NOT NULL AND e.endDate < :today")
List<Event> findExpiredOngoingEvents(@Param("today") String today);
}
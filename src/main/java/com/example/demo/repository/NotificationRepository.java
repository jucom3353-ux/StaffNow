package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.User;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalse(User user);

    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    int countByUserAndIsReadFalse(User user);
    
    void deleteByUser(User user);
}
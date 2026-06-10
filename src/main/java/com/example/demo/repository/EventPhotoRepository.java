// EventPhotoRepository.java
package com.example.demo.repository;

import com.example.demo.entity.EventPhoto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {

    List<EventPhoto> findByUserOrderByCreatedAtDesc(User user);

    int countByUser(User user);
}
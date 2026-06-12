// EventPhotoRepository.java
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.EventPhoto;
import com.example.demo.entity.User;

public interface EventPhotoRepository extends JpaRepository<EventPhoto, Long> {

    List<EventPhoto> findByUserOrderByCreatedAtDesc(User user);

    int countByUser(User user);
}
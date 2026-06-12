package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.PreferredWorkTime;
import com.example.demo.entity.User;


public interface PreferredWorkTimeRepository extends JpaRepository<PreferredWorkTime, Long> {

    List<PreferredWorkTime> findByUser(User user);

    void deleteByUser(User user);
}
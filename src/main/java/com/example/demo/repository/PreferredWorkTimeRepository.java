package com.example.demo.repository;

import com.example.demo.entity.PreferredWorkTime;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PreferredWorkTimeRepository extends JpaRepository<PreferredWorkTime, Long> {

    List<PreferredWorkTime> findByUser(User user);

    void deleteByUser(User user);
}
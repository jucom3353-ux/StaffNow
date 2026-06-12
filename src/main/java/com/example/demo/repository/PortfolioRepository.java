package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Portfolio;
import com.example.demo.entity.User;


public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserOrderByCreatedAtDesc(User user);
    int countByUser(User user);
}
package com.example.demo.repository;

import com.example.demo.entity.Portfolio;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserOrderByCreatedAtDesc(User user);
    int countByUser(User user);
}
package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Portfolio;
import com.example.demo.entity.PortfolioImage;


public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findByPortfolioOrderBySortOrderAsc(Portfolio portfolio);
    int countByPortfolio(Portfolio portfolio);
    void deleteByPortfolio(Portfolio portfolio);
}
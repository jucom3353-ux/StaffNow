package com.example.demo.repository;

import com.example.demo.entity.Portfolio;
import com.example.demo.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findByPortfolioOrderBySortOrderAsc(Portfolio portfolio);
    int countByPortfolio(Portfolio portfolio);
    void deleteByPortfolio(Portfolio portfolio);
}
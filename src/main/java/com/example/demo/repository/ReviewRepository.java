package com.example.demo.repository;

import com.example.demo.entity.Review;
import com.example.demo.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    List<Review> findByWorker(User worker);

    boolean existsByApplicationId(Long applicationId);
}
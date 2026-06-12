package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Mileage;
import com.example.demo.entity.User;


public interface MileageRepository extends JpaRepository<Mileage, Long> {
    List<Mileage> findByUserOrderByCreatedAtDesc(User user);
@Query("SELECT COALESCE(SUM(m.amount), 0) FROM Mileage m WHERE m.amount > 0")
long sumTotalIssuedMileage();
}
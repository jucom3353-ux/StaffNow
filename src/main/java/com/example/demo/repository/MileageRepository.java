package com.example.demo.repository;

import com.example.demo.entity.Mileage;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MileageRepository extends JpaRepository<Mileage, Long> {
    List<Mileage> findByUserOrderByCreatedAtDesc(User user);
@Query("SELECT COALESCE(SUM(m.amount), 0) FROM Mileage m WHERE m.amount > 0")
long sumTotalIssuedMileage();
}
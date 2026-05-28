package com.example.demo.repository;

import com.example.demo.entity.Mileage;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MileageRepository extends JpaRepository<Mileage, Long> {
    List<Mileage> findByUserOrderByCreatedAtDesc(User user);
}
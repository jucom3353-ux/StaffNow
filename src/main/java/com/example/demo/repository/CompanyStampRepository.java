package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CompanyStamp;
import com.example.demo.entity.User;


public interface CompanyStampRepository extends JpaRepository<CompanyStamp, Long> {
    Optional<CompanyStamp> findByUser(User user);
}
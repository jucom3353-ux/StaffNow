package com.example.demo.repository;

import com.example.demo.entity.CompanyStamp;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyStampRepository extends JpaRepository<CompanyStamp, Long> {
    Optional<CompanyStamp> findByUser(User user);
}
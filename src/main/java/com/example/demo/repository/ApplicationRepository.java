package com.example.demo.repository;

import com.example.demo.entity.Application;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository
        extends JpaRepository<Application, Long> {
}
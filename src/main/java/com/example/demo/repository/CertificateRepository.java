package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Certificate;
import com.example.demo.entity.Resume;


public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByResume(Resume resume);
    Optional<Certificate> findByIdAndResume(Long id, Resume resume);
}
package com.example.demo.repository;

import com.example.demo.entity.Certificate;
import com.example.demo.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByResume(Resume resume);
    Optional<Certificate> findByIdAndResume(Long id, Resume resume);
}
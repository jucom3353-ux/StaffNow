package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Career;
import com.example.demo.entity.Resume;


public interface CareerRepository extends JpaRepository<Career, Long> {
    List<Career> findByResume(Resume resume);
    Optional<Career> findByIdAndResume(Long id, Resume resume);
    List<Career> findByResumeIn(List<Resume> resumes);
}
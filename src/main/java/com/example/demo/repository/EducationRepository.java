package com.example.demo.repository;

import com.example.demo.entity.Education;
import com.example.demo.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByResume(Resume resume);
    Optional<Education> findByIdAndResume(Long id, Resume resume);
}
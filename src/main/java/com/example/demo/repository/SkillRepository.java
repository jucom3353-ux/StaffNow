package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.JobCategory;
import com.example.demo.entity.Skill;
import com.example.demo.entity.User;


public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByUser(User user);

    // 변경: String → JobCategory
    List<Skill> findByUserAndCategory(User user, JobCategory category);

    boolean existsByUserAndName(User user, String name);

    Optional<Skill> findByIdAndUser(Long id, User user);

    // 추가: 카테고리로 유저 목록 조회 (자동매칭용)
    List<Skill> findByCategory(JobCategory category);

    List<Skill> findByUserIn(List<User> users);
}
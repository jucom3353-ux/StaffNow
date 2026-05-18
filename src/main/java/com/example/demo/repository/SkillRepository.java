package com.example.demo.repository;

import com.example.demo.entity.Skill;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    List<Skill> findByUser(User user);

    List<Skill> findByUserAndCategory(User user, String category);

    boolean existsByUserAndName(User user, String name);

    Optional<Skill> findByIdAndUser(Long id, User user);
}
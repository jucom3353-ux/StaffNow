package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Faq;
import com.example.demo.entity.FaqCategory;
import com.example.demo.entity.FaqTarget;


public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByIsActiveTrueOrderByOrderIndexAsc();
    List<Faq> findByCategoryAndIsActiveTrueOrderByOrderIndexAsc(FaqCategory category);
    List<Faq> findByTargetAndIsActiveTrueOrderByOrderIndexAsc(FaqTarget target);
    List<Faq> findByQuestionContainingAndIsActiveTrue(String keyword);
}
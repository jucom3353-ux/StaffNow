package com.example.demo.repository;

import com.example.demo.entity.Faq;
import com.example.demo.entity.FaqCategory;
import com.example.demo.entity.FaqTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByIsActiveTrueOrderByOrderIndexAsc();
    List<Faq> findByCategoryAndIsActiveTrueOrderByOrderIndexAsc(FaqCategory category);
    List<Faq> findByTargetAndIsActiveTrueOrderByOrderIndexAsc(FaqTarget target);
    List<Faq> findByQuestionContainingAndIsActiveTrue(String keyword);
}
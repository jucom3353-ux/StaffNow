package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Inquiry;
import com.example.demo.entity.InquiryStatus;
import com.example.demo.entity.InquiryType;
import com.example.demo.entity.User;


public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByUserOrderByCreatedAtDesc(User user);
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);
    List<Inquiry> findByTypeOrderByCreatedAtDesc(InquiryType type);
    List<Inquiry> findByTypeAndStatusOrderByCreatedAtDesc(
            InquiryType type, InquiryStatus status);
}
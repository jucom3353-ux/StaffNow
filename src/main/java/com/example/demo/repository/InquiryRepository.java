package com.example.demo.repository;

import com.example.demo.entity.Inquiry;
import com.example.demo.entity.InquiryStatus;
import com.example.demo.entity.InquiryType;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findByUserOrderByCreatedAtDesc(User user);
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);
    List<Inquiry> findByTypeOrderByCreatedAtDesc(InquiryType type);
    List<Inquiry> findByTypeAndStatusOrderByCreatedAtDesc(
            InquiryType type, InquiryStatus status);
}
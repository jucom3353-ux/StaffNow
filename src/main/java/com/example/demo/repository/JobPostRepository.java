package com.example.demo.repository;

import com.example.demo.entity.JobPost; // 엔티티 패키지 경로 확인 필요
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// 1. class 대신 interface로 변경
// 2. JpaRepository<엔티티클래스명, ID타입>를 상속받아야 함
public interface JobPostRepository extends JpaRepository<JobPost, Long> {
    // 여기에 별도의 메서드를 작성하지 않아도 save(), findAll(), findById() 등을 사용할 수 있습니다.
}
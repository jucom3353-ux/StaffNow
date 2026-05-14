package com.example.demo.repository;

import com.example.demo.entity.Contract;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // 기업 기준 조회
    List<Contract> findByCompany(User company);

    // 근로자 기준 조회
    List<Contract> findByWorker(User worker);
}
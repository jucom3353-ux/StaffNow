package com.example.demo.repository;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByCompany(User company);

    List<Contract> findByWorker(User worker);

    List<Contract> findByCompanyAndWorker(User company, User worker);

    // 기존: 만료 처리
    @Modifying
    @Query("UPDATE Contract c SET c.status = 'EXPIRED' " +
           "WHERE c.status = 'PENDING' AND c.createdAt < :expiredTime")
    void expireOldContracts(@Param("expiredTime") LocalDateTime expiredTime);

    // 신규: 1개월 미작성 → FAILED 처리 대상 조회
    // 생성 후 1개월 지났는데 양쪽 모두 서명 안 된 계약서
    @Query("SELECT c FROM Contract c WHERE c.status = 'PENDING' " +
           "AND c.createdAt < :expiredTime " +
           "AND (c.companySignedAt IS NULL OR c.workerSignedAt IS NULL)")
    List<Contract> findUnsignedExpiredContracts(
            @Param("expiredTime") LocalDateTime expiredTime
    );

    // 신규: 완료 후 1년 지난 계약서 조회 (다운로드 제한용)
    @Query("SELECT c FROM Contract c WHERE c.status = 'COMPLETED' " +
           "AND c.workerSignedAt < :expiredTime")
    List<Contract> findExpiredCompletedContracts(
            @Param("expiredTime") LocalDateTime expiredTime
    );
}
package com.example.demo.repository;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
    long countByStatus(ContractStatus status);

    @Query("SELECT c FROM Contract c WHERE c.status = 'PENDING' " +
           "AND c.createdAt < :expiredTime")
    List<Contract> findUnsignedExpiredContracts(
            @Param("expiredTime") LocalDateTime expiredTime);

    @Query("SELECT c FROM Contract c WHERE c.status = 'SIGNED' " +
           "AND c.workerSignedAt < :expiredTime")
    List<Contract> findDownloadExpiredContracts(
            @Param("expiredTime") LocalDateTime expiredTime);

    @Query("SELECT c FROM Contract c WHERE c.status = 'PENDING' " +
           "AND c.createdAt >= :from AND c.createdAt <= :to")
    List<Contract> findPendingContractsCreatedAt(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
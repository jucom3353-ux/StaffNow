package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공고 연결
    @ManyToOne
    @JoinColumn(name = "job_post_id")
    private JobPost jobPost;

    // 기업 (갑)
    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;

    // 근로자 (을)
    @ManyToOne
    @JoinColumn(name = "worker_id")
    private User worker;

    // 근로계약기간
    private String contractStartDate;
    private String contractEndDate;

    // 계약 상태
    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.PENDING;

    // 서명 시각
    private LocalDateTime companySignedAt;
    private LocalDateTime workerSignedAt;

    // 생성일
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public JobPost getJobPost() { return jobPost; }
    public User getCompany() { return company; }
    public User getWorker() { return worker; }
    public String getContractStartDate() { return contractStartDate; }
    public String getContractEndDate() { return contractEndDate; }
    public ContractStatus getStatus() { return status; }
    public LocalDateTime getCompanySignedAt() { return companySignedAt; }
    public LocalDateTime getWorkerSignedAt() { return workerSignedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }
    public void setCompany(User company) { this.company = company; }
    public void setWorker(User worker) { this.worker = worker; }
    public void setContractStartDate(String contractStartDate) { this.contractStartDate = contractStartDate; }
    public void setContractEndDate(String contractEndDate) { this.contractEndDate = contractEndDate; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public void setCompanySignedAt(LocalDateTime companySignedAt) { this.companySignedAt = companySignedAt; }
    public void setWorkerSignedAt(LocalDateTime workerSignedAt) { this.workerSignedAt = workerSignedAt; }
}
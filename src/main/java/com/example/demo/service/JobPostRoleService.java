package com.example.demo.service;

import com.example.demo.dto.JobPostRoleRequestDto;
import com.example.demo.dto.JobPostRoleResponseDto;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostRole;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.JobPostRoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostRoleService {

    private final JobPostRoleRepository jobPostRoleRepository;
    private final JobPostRepository jobPostRepository;

    public JobPostRoleService(
            JobPostRoleRepository jobPostRoleRepository,
            JobPostRepository jobPostRepository
    ) {
        this.jobPostRoleRepository = jobPostRoleRepository;
        this.jobPostRepository = jobPostRepository;
    }

    // 직무 등록
    @Transactional
    public void createRoles(Long jobPostId,
                            List<JobPostRoleRequestDto> requestDtos,
                            User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 직무를 등록할 수 있습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고에만 직무 등록 가능");
        }

        List<JobPostRole> roles = requestDtos.stream().map(dto -> {
            JobPostRole role = new JobPostRole();
            role.setJobPost(jobPost);
            role.setRoleName(dto.getRoleName());
            role.setWageAmount(dto.getWageAmount());
            role.setRecruitCount(dto.getRecruitCount());
            role.setRequiresExperience(
                    dto.getRequiresExperience() != null && dto.getRequiresExperience()
            );
            return role;
        }).collect(Collectors.toList());

        jobPostRoleRepository.saveAll(roles);
    }

    // 직무 조회
    @Transactional
    public List<JobPostRoleResponseDto> getRoles(Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        return jobPostRoleRepository.findByJobPost(jobPost)
                .stream()
                .map(JobPostRoleResponseDto::new)
                .collect(Collectors.toList());
    }

    // 직무 전체 교체 (수정 시)
    @Transactional
    public void updateRoles(Long jobPostId,
                            List<JobPostRoleRequestDto> requestDtos,
                            User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 직무를 수정할 수 있습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고에만 직무 수정 가능");
        }

        jobPostRoleRepository.deleteByJobPost(jobPost);

        List<JobPostRole> roles = requestDtos.stream().map(dto -> {
            JobPostRole role = new JobPostRole();
            role.setJobPost(jobPost);
            role.setRoleName(dto.getRoleName());
            role.setWageAmount(dto.getWageAmount());
            role.setRecruitCount(dto.getRecruitCount());
            role.setRequiresExperience(
                    dto.getRequiresExperience() != null && dto.getRequiresExperience()
            );
            return role;
        }).collect(Collectors.toList());

        jobPostRoleRepository.saveAll(roles);
    }

    // 직무 삭제
    @Transactional
    public void deleteRoles(Long jobPostId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 직무를 삭제할 수 있습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고에만 직무 삭제 가능");
        }

        jobPostRoleRepository.deleteByJobPost(jobPost);
    }
}
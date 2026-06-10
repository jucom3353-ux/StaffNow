package com.example.demo.service;

import com.example.demo.dto.JobPostRoleRequestDto;
import com.example.demo.dto.JobPostRoleResponseDto;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostRole;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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
            JobPostRepository jobPostRepository) {
        this.jobPostRoleRepository = jobPostRoleRepository;
        this.jobPostRepository = jobPostRepository;
    }

    @Transactional
    public void createRoles(Long jobPostId, List<JobPostRoleRequestDto> requestDtos,
                            User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        List<JobPostRole> roles = requestDtos.stream().map(dto -> {
            JobPostRole role = new JobPostRole();
            role.setJobPost(jobPost);
            role.setRoleName(dto.getRoleName());
            role.setWageAmount(dto.getWageAmount());
            role.setRecruitCount(dto.getRecruitCount());
            role.setRequiresExperience(
                    dto.getRequiresExperience() != null && dto.getRequiresExperience());
            return role;
        }).collect(Collectors.toList());

        jobPostRoleRepository.saveAll(roles);
    }

    @Transactional
    public List<JobPostRoleResponseDto> getRoles(Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        return jobPostRoleRepository.findByJobPost(jobPost).stream()
                .map(JobPostRoleResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void updateRoles(Long jobPostId, List<JobPostRoleRequestDto> requestDtos,
                            User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        jobPostRoleRepository.deleteByJobPost(jobPost);

        List<JobPostRole> roles = requestDtos.stream().map(dto -> {
            JobPostRole role = new JobPostRole();
            role.setJobPost(jobPost);
            role.setRoleName(dto.getRoleName());
            role.setWageAmount(dto.getWageAmount());
            role.setRecruitCount(dto.getRecruitCount());
            role.setRequiresExperience(
                    dto.getRequiresExperience() != null && dto.getRequiresExperience());
            return role;
        }).collect(Collectors.toList());

        jobPostRoleRepository.saveAll(roles);
    }

    @Transactional
    public void deleteRoles(Long jobPostId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        jobPostRoleRepository.deleteByJobPost(jobPost);
    }
}
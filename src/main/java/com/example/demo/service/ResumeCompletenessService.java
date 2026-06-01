package com.example.demo.service;

import com.example.demo.dto.ResumeCompletenessResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeCompletenessService {

    private final ResumeRepository resumeRepository;
    private final SkillRepository skillRepository;
    private final EducationRepository educationRepository;
    private final CareerRepository careerRepository;
    private final CertificateRepository certificateRepository;

    @Transactional(readOnly = true)
    public ResumeCompletenessResponseDto getCompleteness(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        List<String> completed = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        int totalItems = 0;
        int completedCount = 0;

        // 1. 기본 프로필 (User)
        totalItems++;
        if (loginUser.getName() != null && !loginUser.getName().isBlank()) {
            completed.add("이름"); completedCount++;
        } else missing.add("이름");

        totalItems++;
        if (loginUser.getPhone() != null && !loginUser.getPhone().isBlank()) {
            completed.add("연락처"); completedCount++;
        } else missing.add("연락처");

        totalItems++;
        if (loginUser.getProfileImageUrl() != null) {
            completed.add("프로필 사진"); completedCount++;
        } else missing.add("프로필 사진");

        totalItems++;
        if (loginUser.getBio() != null && !loginUser.getBio().isBlank()) {
            completed.add("자기소개"); completedCount++;
        } else missing.add("자기소개");

        totalItems++;
        if (loginUser.getActivityRegion() != null && !loginUser.getActivityRegion().isBlank()) {
            completed.add("활동 지역"); completedCount++;
        } else missing.add("활동 지역");

        totalItems++;
        if (loginUser.getMbti() != null && !loginUser.getMbti().isBlank()) {
            completed.add("MBTI"); completedCount++;
        } else missing.add("MBTI");

        // 2. 이력서
        Resume resume = resumeRepository.findByUser(loginUser).orElse(null);

        totalItems++;
        if (resume != null && resume.getDesiredJob() != null
                && !resume.getDesiredJob().isBlank()) {
            completed.add("희망 직종"); completedCount++;
        } else missing.add("희망 직종");

        totalItems++;
        if (resume != null && resume.getDesiredLocation() != null
                && !resume.getDesiredLocation().isBlank()) {
            completed.add("희망 근무지"); completedCount++;
        } else missing.add("희망 근무지");

        totalItems++;
        if (resume != null && resume.getDesiredSalary() != null
                && !resume.getDesiredSalary().isBlank()) {
            completed.add("희망 급여"); completedCount++;
        } else missing.add("희망 급여");

        // 3. 스킬
        totalItems++;
        List<Skill> skills = skillRepository.findByUser(loginUser);
        if (!skills.isEmpty()) {
            completed.add("스킬 (" + skills.size() + "개)"); completedCount++;
        } else missing.add("스킬");

        // 4. 학력
        totalItems++;
        if (resume != null) {
            List<Education> educations = educationRepository.findByResume(resume);
            if (!educations.isEmpty()) {
                completed.add("학력"); completedCount++;
            } else missing.add("학력");
        } else missing.add("학력");

        // 5. 경력
        totalItems++;
        if (resume != null) {
            List<Career> careers = careerRepository.findByResume(resume);
            if (!careers.isEmpty()) {
                completed.add("경력"); completedCount++;
            } else missing.add("경력 (없으면 '경력 없음' 표시 권장)");
        } else missing.add("경력");

        // 6. 자격증
        totalItems++;
        if (resume != null) {
            List<Certificate> certificates = certificateRepository.findByResume(resume);
            if (!certificates.isEmpty()) {
                completed.add("자격증"); completedCount++;
            } else missing.add("자격증 (없으면 생략 가능)");
        } else missing.add("자격증");

        // 7. 비상연락망
        totalItems++;
        if (loginUser.getEmergencyContactName() != null
                && !loginUser.getEmergencyContactName().isBlank()) {
            completed.add("비상연락망"); completedCount++;
        } else missing.add("비상연락망");

        int percent = (int) Math.round((double) completedCount / totalItems * 100);

        return ResumeCompletenessResponseDto.builder()
                .completionPercent(percent)
                .completedItems(completed)
                .missingItems(missing)
                .build();
    }
}
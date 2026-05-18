package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final EducationRepository educationRepository;
    private final CareerRepository careerRepository;
    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;

    // 이력서 조회 (없으면 빈 이력서 생성)
    @Transactional
    public ResumeResponseDto getMyResume(User loginUser) {
        Resume resume = resumeRepository.findByUser(loginUser)
                .orElseGet(() -> {
                    Resume newResume = new Resume();
                    newResume.setUser(loginUser);
                    return resumeRepository.save(newResume);
                });

        return buildResponse(resume);
    }

    // 이력서 수정 (희망 근무 조건 + 취업우대사항)
    @Transactional
    public ResumeResponseDto updateResume(ResumeRequestDto requestDto, User loginUser) {
        Resume resume = resumeRepository.findByUser(loginUser)
                .orElseGet(() -> {
                    Resume newResume = new Resume();
                    newResume.setUser(loginUser);
                    return resumeRepository.save(newResume);
                });

        if (requestDto.getDesiredLocation() != null) resume.setDesiredLocation(requestDto.getDesiredLocation());
        if (requestDto.getDesiredJob() != null) resume.setDesiredJob(requestDto.getDesiredJob());
        if (requestDto.getWorkType() != null) resume.setWorkType(requestDto.getWorkType());
        if (requestDto.getWorkPeriod() != null) resume.setWorkPeriod(requestDto.getWorkPeriod());
        if (requestDto.getWorkSchedule() != null) resume.setWorkSchedule(requestDto.getWorkSchedule());
        if (requestDto.getDesiredSalary() != null) resume.setDesiredSalary(requestDto.getDesiredSalary());
        if (requestDto.getDisability() != null) resume.setDisability(requestDto.getDisability());
        if (requestDto.getVeteranStatus() != null) resume.setVeteranStatus(requestDto.getVeteranStatus());
        if (requestDto.getEmploymentSupport() != null) resume.setEmploymentSupport(requestDto.getEmploymentSupport());
        if (requestDto.getMilitaryService() != null) resume.setMilitaryService(requestDto.getMilitaryService());

        resumeRepository.save(resume);
        return buildResponse(resume);
    }

    // 학력 추가
    @Transactional
    public ResumeResponseDto addEducation(EducationRequestDto requestDto, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);

        Education education = new Education();
        education.setResume(resume);
        education.setSchoolName(requestDto.getSchoolName());
        education.setMajor(requestDto.getMajor());
        education.setEnrollDate(requestDto.getEnrollDate());
        education.setGraduateDate(requestDto.getGraduateDate());
        education.setGraduateStatus(requestDto.getGraduateStatus());
        educationRepository.save(education);

        return buildResponse(resume);
    }

    // 학력 삭제
    @Transactional
    public void deleteEducation(Long educationId, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);
        Education education = educationRepository.findByIdAndResume(educationId, resume)
                .orElseThrow(() -> new RuntimeException("학력 없음 또는 권한 없음"));
        educationRepository.delete(education);
    }

    // 경력 추가
    @Transactional
    public ResumeResponseDto addCareer(CareerRequestDto requestDto, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);

        Career career = new Career();
        career.setResume(resume);
        career.setCompanyName(requestDto.getCompanyName());
        career.setJobTitle(requestDto.getJobTitle());
        career.setJoinDate(requestDto.getJoinDate());
        career.setLeaveDate(requestDto.getLeaveDate());
        career.setIsCurrent(requestDto.getIsCurrent());
        careerRepository.save(career);

        return buildResponse(resume);
    }

    // 경력 삭제
    @Transactional
    public void deleteCareer(Long careerId, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);
        Career career = careerRepository.findByIdAndResume(careerId, resume)
                .orElseThrow(() -> new RuntimeException("경력 없음 또는 권한 없음"));
        careerRepository.delete(career);
    }

    // 자격증 추가
    @Transactional
    public ResumeResponseDto addCertificate(CertificateRequestDto requestDto, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);

        Certificate certificate = new Certificate();
        certificate.setResume(resume);
        certificate.setName(requestDto.getName());
        certificate.setIssuer(requestDto.getIssuer());
        certificate.setAcquiredDate(requestDto.getAcquiredDate());
        certificateRepository.save(certificate);

        return buildResponse(resume);
    }

    // 자격증 삭제
    @Transactional
    public void deleteCertificate(Long certificateId, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);
        Certificate certificate = certificateRepository.findByIdAndResume(certificateId, resume)
                .orElseThrow(() -> new RuntimeException("자격증 없음 또는 권한 없음"));
        certificateRepository.delete(certificate);
    }

    // 특정 유저 이력서 조회 (기업용)
    @Transactional(readOnly = true)
    public ResumeResponseDto getUserResume(Long userId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 조회 가능합니다.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Resume resume = resumeRepository.findByUser(targetUser)
                .orElseThrow(() -> new RuntimeException("이력서 없음"));

        return buildResponse(resume);
    }

    private Resume getOrCreateResume(User loginUser) {
        return resumeRepository.findByUser(loginUser)
                .orElseGet(() -> {
                    Resume newResume = new Resume();
                    newResume.setUser(loginUser);
                    return resumeRepository.save(newResume);
                });
    }

    private ResumeResponseDto buildResponse(Resume resume) {
        List<Education> educations = educationRepository.findByResume(resume);
        List<Career> careers = careerRepository.findByResume(resume);
        List<Certificate> certificates = certificateRepository.findByResume(resume);
        return new ResumeResponseDto(resume, educations, careers, certificates);
    }
}
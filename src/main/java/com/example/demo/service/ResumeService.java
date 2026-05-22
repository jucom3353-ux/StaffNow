package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    @Transactional
    public void deleteEducation(Long educationId, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);
        Education education = educationRepository.findByIdAndResume(educationId, resume)
                .orElseThrow(() -> new CustomException(ErrorCode.EDUCATION_NOT_FOUND));
        educationRepository.delete(education);
    }

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

    @Transactional
    public void deleteCareer(Long careerId, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);
        Career career = careerRepository.findByIdAndResume(careerId, resume)
                .orElseThrow(() -> new CustomException(ErrorCode.CAREER_NOT_FOUND));
        careerRepository.delete(career);
    }

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

    @Transactional
    public void deleteCertificate(Long certificateId, User loginUser) {
        Resume resume = getOrCreateResume(loginUser);
        Certificate certificate = certificateRepository.findByIdAndResume(certificateId, resume)
                .orElseThrow(() -> new CustomException(ErrorCode.CERTIFICATE_NOT_FOUND));
        certificateRepository.delete(certificate);
    }

    @Transactional(readOnly = true)
    public ResumeResponseDto getUserResume(Long userId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Resume resume = resumeRepository.findByUser(targetUser)
                .orElseThrow(() -> new CustomException(ErrorCode.RESUME_NOT_FOUND));

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
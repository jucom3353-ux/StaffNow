package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.JobPostCreateRequestDto;
import com.example.demo.dto.JobPostTemplateRequestDto;
import com.example.demo.dto.JobPostTemplateResponseDto;
import com.example.demo.entity.JobCategory;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostTemplate;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.JobCategoryRepository;
import com.example.demo.repository.JobPostTemplateRepository;
import com.example.demo.util.AuthorizationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobPostTemplateService {

    private final JobPostTemplateRepository jobPostTemplateRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final JobPostService jobPostService;

    @Transactional
    public JobPostTemplateResponseDto createTemplate(
            JobPostTemplateRequestDto requestDto, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPostTemplate template = new JobPostTemplate();
        applyFields(template, requestDto, loginUser);

        return new JobPostTemplateResponseDto(
                jobPostTemplateRepository.save(template));
    }

    @Transactional
    public JobPostTemplateResponseDto createTemplateFromJobPost(
            JobPost jobPost, String templateName, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPostTemplate template = new JobPostTemplate();
        template.setUser(loginUser);
        template.setTemplateName(templateName);
        template.setTitle(jobPost.getTitle());
        template.setWorkLocation(jobPost.getWorkLocation());
        template.setStartTime(jobPost.getStartTime());
        template.setEndTime(jobPost.getEndTime());
        template.setBreakTime(jobPost.getBreakTime());
        template.setWageType(jobPost.getWageType());
        template.setWageAmount(jobPost.getWageAmount());
        template.setIncludeHolidayPay(jobPost.getIncludeHolidayPay());
        template.setWorkType(jobPost.getWorkType());
        template.setDescription(jobPost.getDescription());
        template.setContent(jobPost.getContent());
        template.setRequiredPersonality(jobPost.getRequiredPersonality());
        template.setRequiredCondition(jobPost.getRequiredCondition());
        template.setPreferredExperience(jobPost.getPreferredExperience());
        template.setUniformInfo(jobPost.getUniformInfo());
        template.setManagerName(jobPost.getManagerName());
        template.setManagerPhone(jobPost.getManagerPhone());
        template.setManagerEmail(jobPost.getManagerEmail());
        template.setMealProvided(jobPost.getMealProvided());
        template.setAllowOnline(jobPost.getAllowOnline());
        template.setAllowPhone(jobPost.getAllowPhone());
        template.setAllowSms(jobPost.getAllowSms());
        template.setCategory(jobPost.getCategory());

        return new JobPostTemplateResponseDto(
                jobPostTemplateRepository.save(template));
    }

    @Transactional
    public JobPostTemplateResponseDto updateTemplate(
            Long templateId, JobPostTemplateRequestDto requestDto, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPostTemplate template = jobPostTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

        if (!template.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        applyFields(template, requestDto, loginUser);

        return new JobPostTemplateResponseDto(
                jobPostTemplateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public List<JobPostTemplateResponseDto> getMyTemplates(User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);
        return jobPostTemplateRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream().map(JobPostTemplateResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void createJobPostFromTemplate(Long templateId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPostTemplate template = jobPostTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

        if (!template.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        JobPostCreateRequestDto requestDto = new JobPostCreateRequestDto();
        requestDto.setTitle(template.getTitle());
        requestDto.setWorkLocation(template.getWorkLocation());
        requestDto.setStartTime(template.getStartTime());
        requestDto.setEndTime(template.getEndTime());
        requestDto.setBreakTime(template.getBreakTime());
        requestDto.setWageType(template.getWageType());
        requestDto.setWageAmount(template.getWageAmount());
        requestDto.setIncludeHolidayPay(template.getIncludeHolidayPay());
        requestDto.setWorkType(template.getWorkType());
        requestDto.setDescription(template.getDescription());
        requestDto.setContent(template.getContent());
        requestDto.setRequiredPersonality(template.getRequiredPersonality());
        requestDto.setRequiredCondition(template.getRequiredCondition());
        requestDto.setPreferredExperience(template.getPreferredExperience());
        requestDto.setUniformInfo(template.getUniformInfo());
        requestDto.setManagerName(template.getManagerName());
        requestDto.setManagerPhone(template.getManagerPhone());
        requestDto.setManagerEmail(template.getManagerEmail());
        requestDto.setMealProvided(template.getMealProvided());
        requestDto.setAllowOnline(template.getAllowOnline());
        requestDto.setAllowPhone(template.getAllowPhone());
        requestDto.setAllowSms(template.getAllowSms());
        if (template.getCategory() != null) {
            requestDto.setCategoryId(template.getCategory().getId());
        }

        jobPostService.createJobPost(requestDto, loginUser);
    }

    @Transactional
    public void deleteTemplate(Long templateId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPostTemplate template = jobPostTemplateRepository.findById(templateId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));

        if (!template.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        jobPostTemplateRepository.delete(template);
    }

    private void applyFields(JobPostTemplate template,
                             JobPostTemplateRequestDto dto, User loginUser) {
        template.setUser(loginUser);
        template.setTemplateName(dto.getTemplateName());
        template.setTitle(dto.getTitle());
        template.setWorkLocation(dto.getWorkLocation());
        template.setStartTime(dto.getStartTime());
        template.setEndTime(dto.getEndTime());
        template.setBreakTime(dto.getBreakTime());
        template.setWageType(dto.getWageType());
        template.setWageAmount(dto.getWageAmount());
        template.setIncludeHolidayPay(dto.getIncludeHolidayPay());
        template.setWorkType(dto.getWorkType());
        template.setDescription(dto.getDescription());
        template.setContent(dto.getContent());
        template.setRequiredPersonality(dto.getRequiredPersonality());
        template.setRequiredCondition(dto.getRequiredCondition());
        template.setPreferredExperience(dto.getPreferredExperience());
        template.setUniformInfo(dto.getUniformInfo());
        template.setManagerName(dto.getManagerName());
        template.setManagerPhone(dto.getManagerPhone());
        template.setManagerEmail(dto.getManagerEmail());
        template.setMealProvided(dto.getMealProvided() != null ? dto.getMealProvided() : false);
        template.setAllowOnline(dto.getAllowOnline() != null ? dto.getAllowOnline() : true);
        template.setAllowPhone(dto.getAllowPhone() != null ? dto.getAllowPhone() : false);
        template.setAllowSms(dto.getAllowSms() != null ? dto.getAllowSms() : false);

        if (dto.getCategoryId() != null) {
            JobCategory category = jobCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            template.setCategory(category);
        }
    }
}
package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.JobPostQuestionAnswerRequestDto;
import com.example.demo.dto.JobPostQuestionAnswerResponseDto;
import com.example.demo.dto.JobPostQuestionRequestDto;
import com.example.demo.dto.JobPostQuestionResponseDto;
import com.example.demo.entity.Application;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.JobPostQuestion;
import com.example.demo.entity.JobPostQuestionAnswer;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.JobPostQuestionAnswerRepository;
import com.example.demo.repository.JobPostQuestionRepository;
import com.example.demo.repository.JobPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobPostQuestionService {

    private final JobPostQuestionRepository questionRepository;
    private final JobPostQuestionAnswerRepository answerRepository;
    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;

    // 사전질문 등록 (기존 전체 교체)
    @Transactional
    public List<JobPostQuestionResponseDto> saveQuestions(
            Long jobPostId, JobPostQuestionRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        // 기존 질문 전체 삭제 후 재저장
        questionRepository.deleteByJobPost(jobPost);

        List<JobPostQuestion> questions = requestDto.getQuestions().stream()
                .map(item -> {
                    JobPostQuestion q = new JobPostQuestion();
                    q.setJobPost(jobPost);
                    q.setQuestion(item.getQuestion());
                    q.setRequired(item.isRequired());
                    q.setOrderIndex(item.getOrderIndex());
                    return questionRepository.save(q);
                })
                .collect(Collectors.toList());

        return questions.stream()
                .map(JobPostQuestionResponseDto::new)
                .collect(Collectors.toList());
    }

    // 사전질문 조회
    @Transactional(readOnly = true)
    public List<JobPostQuestionResponseDto> getQuestions(Long jobPostId) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        return questionRepository.findByJobPostOrderByOrderIndexAsc(jobPost)
                .stream()
                .map(JobPostQuestionResponseDto::new)
                .collect(Collectors.toList());
    }

    // 사전질문 답변 제출 (지원 시)
    @Transactional
    public List<JobPostQuestionAnswerResponseDto> submitAnswers(
            Long applicationId,
            JobPostQuestionAnswerRequestDto requestDto,
            User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_APPLICATION);
        }

        List<JobPostQuestionAnswer> answers = requestDto.getAnswers().stream()
                .map(item -> {
                    JobPostQuestion question = questionRepository.findById(item.getQuestionId())
                            .orElseThrow(() -> new CustomException(
                                    ErrorCode.QUESTION_NOT_FOUND));

                    JobPostQuestionAnswer answer = answerRepository
                            .findByQuestionAndApplication(question, application)
                            .orElse(new JobPostQuestionAnswer());

                    answer.setQuestion(question);
                    answer.setApplication(application);
                    answer.setAnswer(item.getAnswer());
                    return answerRepository.save(answer);
                })
                .collect(Collectors.toList());

        return answers.stream()
                .map(JobPostQuestionAnswerResponseDto::new)
                .collect(Collectors.toList());
    }

    // 지원자 답변 조회 (기업용)
    @Transactional(readOnly = true)
    public List<JobPostQuestionAnswerResponseDto> getAnswers(
            Long applicationId, User loginUser) {

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getJobPost().getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        return answerRepository.findByApplication(application)
                .stream()
                .map(JobPostQuestionAnswerResponseDto::new)
                .collect(Collectors.toList());
    }
}
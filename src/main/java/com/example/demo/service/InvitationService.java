package com.example.demo.service;

import com.example.demo.dto.InvitationResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private void validateCompanyOrManager(User user) {
        if (user.getRole() != Role.COMPANY && user.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
    }

    private boolean isMyJobPost(JobPost post, User loginUser) {
        Long companyId = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany().getId()
                : loginUser.getId();
        return post.getUser().getId().equals(companyId) ||
               post.getUser().getId().equals(loginUser.getId());
    }

    @Transactional
    public void sendInvitation(Long jobPostId, Long workerId, User loginUser) {
        validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!isMyJobPost(jobPost, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (worker.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        User companyUser = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;

        if (invitationRepository.existsByCompanyAndWorkerAndJobPost(companyUser, worker, jobPost)) {
            throw new CustomException(ErrorCode.ALREADY_INVITED);
        }

        Invitation invitation = new Invitation();
        invitation.setCompany(companyUser);
        invitation.setWorker(worker);
        invitation.setJobPost(jobPost);
        invitationRepository.save(invitation);

        notificationService.send(worker, NotificationType.INVITATION_RECEIVED,
                "[" + jobPost.getTitle() + "] 기업에서 초대가 왔습니다.", invitation.getId());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getMyInvitations(User loginUser) {
        return invitationRepository.findByWorker(loginUser).stream()
                .map(InvitationResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getSentInvitations(User loginUser) {
        validateCompanyOrManager(loginUser);
        User companyUser = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;
        return invitationRepository.findByCompany(companyUser).stream()
                .map(InvitationResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvitationResponseDto getInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVITATION_NOT_FOUND));

        User companyUser = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;

        if (!invitation.getCompany().getId().equals(companyUser.getId()) &&
            !invitation.getWorker().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return new InvitationResponseDto(invitation);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getMyInvitationsByStatus(
            User loginUser, InvitationStatus status) {
        return invitationRepository.findByWorkerAndStatus(loginUser, status).stream()
                .map(InvitationResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void acceptInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVITATION_NOT_FOUND));

        if (!invitation.getWorker().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 중인 초대만 수락 가능합니다.");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void rejectInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVITATION_NOT_FOUND));

        if (!invitation.getWorker().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 중인 초대만 거절 가능합니다.");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void cancelInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVITATION_NOT_FOUND));

        User companyUser = loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;

        if (!invitation.getCompany().getId().equals(companyUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 중인 초대만 취소 가능합니다.");
        }

        invitationRepository.delete(invitation);
    }
}
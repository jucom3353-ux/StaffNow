package com.example.demo.service;

import com.example.demo.dto.InvitationResponseDto;
import com.example.demo.entity.*;
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

    @Transactional
    public void sendInvitation(Long jobPostId, Long workerId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 초대할 수 있습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고에만 초대할 수 있습니다.");
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("근로자 없음"));

        if (worker.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("구직자만 초대할 수 있습니다.");
        }

        boolean alreadyInvited = invitationRepository
                .existsByCompanyAndWorkerAndJobPost(loginUser, worker, jobPost);

        if (alreadyInvited) {
            throw new RuntimeException("이미 초대한 근로자입니다.");
        }

        Invitation invitation = new Invitation();
        invitation.setCompany(loginUser);
        invitation.setWorker(worker);
        invitation.setJobPost(jobPost);
        invitationRepository.save(invitation);

        // 알림 전송
        notificationService.send(
                worker,
                NotificationType.INVITATION_RECEIVED,
                "[" + jobPost.getTitle() + "] 기업에서 초대가 왔습니다.",
                invitation.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getMyInvitations(User loginUser) {
        return invitationRepository.findByWorker(loginUser)
                .stream()
                .map(InvitationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getSentInvitations(User loginUser) {
        return invitationRepository.findByCompany(loginUser)
                .stream()
                .map(InvitationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvitationResponseDto getInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 없음"));

        if (!invitation.getCompany().getId().equals(loginUser.getId()) &&
            !invitation.getWorker().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 관련 초대만 조회 가능합니다.");
        }

        return new InvitationResponseDto(invitation);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponseDto> getMyInvitationsByStatus(
            User loginUser, InvitationStatus status) {
        return invitationRepository.findByWorkerAndStatus(loginUser, status)
                .stream()
                .map(InvitationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void acceptInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 없음"));

        if (!invitation.getWorker().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인에게 온 초대만 수락 가능합니다.");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("대기 중인 초대만 수락 가능합니다.");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void rejectInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 없음"));

        if (!invitation.getWorker().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인에게 온 초대만 거절 가능합니다.");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("대기 중인 초대만 거절 가능합니다.");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void cancelInvitation(Long invitationId, User loginUser) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("초대 없음"));

        if (!invitation.getCompany().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인이 보낸 초대만 취소 가능합니다.");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("대기 중인 초대만 취소 가능합니다.");
        }

        invitationRepository.delete(invitation);
    }
}
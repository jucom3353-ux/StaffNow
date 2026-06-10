package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.InvitationRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @InjectMocks
    private InvitationService invitationService;

    @Mock private InvitationRepository invitationRepository;
    @Mock private JobPostRepository jobPostRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    // 초대 - INDIVIDUAL이면 예외
    @Test
    void sendInvitation_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        assertThatThrownBy(() -> invitationService.sendInvitation(1L, 2L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업");
    }

    // 초대 - 공고 없음
    @Test
    void sendInvitation_jobPostNotFound_throwsException() {
        User user = makeUser(1L, Role.COMPANY);
        given(jobPostRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.sendInvitation(999L, 2L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("공고를 찾을 수 없습니다");
    }

    // 초대 - 본인 공고 아님
    @Test
    void sendInvitation_notOwner_throwsException() {
        User owner = makeUser(1L, Role.COMPANY);
        User other = makeUser(2L, Role.COMPANY);

        JobPost post = new JobPost();
        post.setUser(owner);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));

        assertThatThrownBy(() -> invitationService.sendInvitation(1L, 3L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("본인 공고");
    }

    // 초대 - 워커 없음
    @Test
    void sendInvitation_workerNotFound_throwsException() {
        User company = makeUser(1L, Role.COMPANY);
        JobPost post = new JobPost();
        post.setUser(company);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.sendInvitation(1L, 999L, company))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    // 초대 - COMPANY 워커에게 초대 불가
    @Test
    void sendInvitation_workerIsCompany_throwsException() {
        User company = makeUser(1L, Role.COMPANY);
        User companyWorker = makeUser(2L, Role.COMPANY);
        JobPost post = new JobPost();
        post.setUser(company);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(2L)).willReturn(Optional.of(companyWorker));

        assertThatThrownBy(() -> invitationService.sendInvitation(1L, 2L, company))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("구직자만");
    }

    // 초대 - 이미 초대
    @Test
    void sendInvitation_alreadyInvited_throwsException() {
        User company = makeUser(1L, Role.COMPANY);
        User worker = makeUser(2L, Role.INDIVIDUAL);
        JobPost post = new JobPost();
        post.setUser(company);

        given(jobPostRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(invitationRepository.existsByCompanyAndWorkerAndJobPost(company, worker, post))
                .willReturn(true);

        assertThatThrownBy(() -> invitationService.sendInvitation(1L, 2L, company))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 초대한");
    }

    // 수락 - 초대 없음
    @Test
    void acceptInvitation_notFound_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        given(invitationRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> invitationService.acceptInvitation(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("초대를 찾을 수 없습니다");
    }

    // 수락 - 본인 초대 아님
    @Test
    void acceptInvitation_notMyInvitation_throwsException() {
        User worker = makeUser(1L, Role.INDIVIDUAL);
        User other = makeUser(2L, Role.INDIVIDUAL);

        Invitation invitation = new Invitation();
        invitation.setWorker(worker);
        invitation.setStatus(InvitationStatus.PENDING);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.acceptInvitation(1L, other))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("접근 권한이 없습니다");
    }

    // 거절 - 이미 처리된 초대
    @Test
    void rejectInvitation_alreadyProcessed_throwsException() {
        User worker = makeUser(1L, Role.INDIVIDUAL);

        Invitation invitation = new Invitation();
        invitation.setWorker(worker);
        invitation.setStatus(InvitationStatus.ACCEPTED);

        given(invitationRepository.findById(1L)).willReturn(Optional.of(invitation));

        assertThatThrownBy(() -> invitationService.rejectInvitation(1L, worker))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("대기 중인 초대만 거절 가능합니다");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
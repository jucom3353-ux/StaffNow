package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkerScrapRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerScrapServiceTest {

    @InjectMocks
    private WorkerScrapService workerScrapService;

    @Mock private WorkerScrapRepository workerScrapRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvitationService invitationService;

    // 스크랩 추가 - INDIVIDUAL이면 예외
    @Test
    void addScrap_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        assertThatThrownBy(() -> workerScrapService.addScrap(2L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업");
    }

    // 스크랩 추가 - 워커 없음
    @Test
    void addScrap_workerNotFound_throwsException() {
        User user = makeUser(1L, Role.COMPANY);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workerScrapService.addScrap(999L, user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    // 스크랩 추가 - 이미 스크랩
    @Test
    void addScrap_alreadyScrapped_throwsException() {
        User company = makeUser(1L, Role.COMPANY);
        User worker = makeUser(2L, Role.INDIVIDUAL);

        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(workerScrapRepository.existsByCompanyAndWorker(company, worker)).willReturn(true);

        assertThatThrownBy(() -> workerScrapService.addScrap(2L, company))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 스크랩한");
    }

    // 스크랩 삭제 - 스크랩 없음
    @Test
    void removeScrap_notFound_throwsException() {
        User company = makeUser(1L, Role.COMPANY);
        User worker = makeUser(2L, Role.INDIVIDUAL);

        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(workerScrapRepository.findByCompanyAndWorker(company, worker))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> workerScrapService.removeScrap(2L, company))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("스크랩을 찾을 수 없습니다");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
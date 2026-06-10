package com.example.demo.service;

import com.example.demo.dto.WorkerBlacklistRequestDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkerBlacklistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerBlacklistServiceTest {

    @InjectMocks
    private WorkerBlacklistService workerBlacklistService;

    @Mock private WorkerBlacklistRepository workerBlacklistRepository;
    @Mock private UserRepository userRepository;

    // 블랙리스트 추가 - INDIVIDUAL이면 예외
    @Test
    void addBlacklist_individualUser_throwsException() {
        User user = makeUser(1L, Role.INDIVIDUAL);
        assertThatThrownBy(() -> workerBlacklistService.addBlacklist(2L, new WorkerBlacklistRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("기업");
    }

    // 블랙리스트 추가 - 워커 없음
    @Test
    void addBlacklist_workerNotFound_throwsException() {
        User user = makeUser(1L, Role.COMPANY);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> workerBlacklistService.addBlacklist(999L, new WorkerBlacklistRequestDto(), user))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    // 블랙리스트 추가 - 이미 블랙리스트
    @Test
    void addBlacklist_alreadyBlacklisted_throwsException() {
        User company = makeUser(1L, Role.COMPANY);
        User worker = makeUser(2L, Role.INDIVIDUAL);

        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(workerBlacklistRepository.existsByCompanyAndWorker(company, worker)).willReturn(true);

        assertThatThrownBy(() -> workerBlacklistService.addBlacklist(2L, new WorkerBlacklistRequestDto(), company))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 채용부적합");
    }

    // 블랙리스트 삭제 - 없음
    @Test
    void removeBlacklist_notFound_throwsException() {
        User company = makeUser(1L, Role.COMPANY);
        User worker = makeUser(2L, Role.INDIVIDUAL);

        given(userRepository.findById(2L)).willReturn(Optional.of(worker));
        given(workerBlacklistRepository.findByCompanyAndWorker(company, worker))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> workerBlacklistService.removeBlacklist(2L, company))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("채용부적합 내역을 찾을 수 없습니다");
    }

    private User makeUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }
}
package com.example.demo.service;

import com.example.demo.dto.WorkerBlacklistRequestDto;
import com.example.demo.dto.WorkerBlacklistResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkerBlacklist;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkerBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerBlacklistService {

    private final WorkerBlacklistRepository workerBlacklistRepository;
    private final UserRepository userRepository;

    // 채용부적합 등록
    @Transactional
    public WorkerBlacklistResponseDto addBlacklist(
            Long workerId, WorkerBlacklistRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (workerBlacklistRepository.existsByCompanyAndWorker(loginUser, worker)) {
            throw new CustomException(ErrorCode.ALREADY_BLACKLISTED);
        }

        WorkerBlacklist blacklist = new WorkerBlacklist();
        blacklist.setCompany(loginUser);
        blacklist.setWorker(worker);
        blacklist.setReason(requestDto.getReason());

        return new WorkerBlacklistResponseDto(workerBlacklistRepository.save(blacklist));
    }

    // 채용부적합 해제
    @Transactional
    public void removeBlacklist(Long workerId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WorkerBlacklist blacklist = workerBlacklistRepository
                .findByCompanyAndWorker(loginUser, worker)
                .orElseThrow(() -> new CustomException(ErrorCode.BLACKLIST_NOT_FOUND));

        workerBlacklistRepository.delete(blacklist);
    }

    // 채용부적합 목록 조회
    @Transactional(readOnly = true)
    public List<WorkerBlacklistResponseDto> getBlacklist(User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        return workerBlacklistRepository.findByCompany(loginUser)
                .stream()
                .map(WorkerBlacklistResponseDto::new)
                .collect(Collectors.toList());
    }
}
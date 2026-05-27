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

    private void validateCompanyOrManager(User user) {
        if (user.getRole() != Role.COMPANY && user.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
    }

    private User getCompanyUser(User loginUser) {
        return loginUser.getRole() == Role.MANAGER
                ? loginUser.getCompany() : loginUser;
    }

    @Transactional
    public WorkerBlacklistResponseDto addBlacklist(
            Long workerId, WorkerBlacklistRequestDto requestDto, User loginUser) {
        validateCompanyOrManager(loginUser);

        User companyUser = getCompanyUser(loginUser);
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (workerBlacklistRepository.existsByCompanyAndWorker(companyUser, worker)) {
            throw new CustomException(ErrorCode.ALREADY_BLACKLISTED);
        }

        WorkerBlacklist blacklist = new WorkerBlacklist();
        blacklist.setCompany(companyUser);
        blacklist.setWorker(worker);
        blacklist.setReason(requestDto.getReason());

        return new WorkerBlacklistResponseDto(workerBlacklistRepository.save(blacklist));
    }

    @Transactional
    public void removeBlacklist(Long workerId, User loginUser) {
        validateCompanyOrManager(loginUser);

        User companyUser = getCompanyUser(loginUser);
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WorkerBlacklist blacklist = workerBlacklistRepository
                .findByCompanyAndWorker(companyUser, worker)
                .orElseThrow(() -> new CustomException(ErrorCode.BLACKLIST_NOT_FOUND));

        workerBlacklistRepository.delete(blacklist);
    }

    @Transactional(readOnly = true)
    public List<WorkerBlacklistResponseDto> getBlacklist(User loginUser) {
        validateCompanyOrManager(loginUser);

        User companyUser = getCompanyUser(loginUser);
        return workerBlacklistRepository.findByCompany(companyUser)
                .stream()
                .map(WorkerBlacklistResponseDto::new)
                .collect(Collectors.toList());
    }
}
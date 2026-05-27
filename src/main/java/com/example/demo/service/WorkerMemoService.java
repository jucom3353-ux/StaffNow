package com.example.demo.service;

import com.example.demo.dto.WorkerMemoRequestDto;
import com.example.demo.dto.WorkerMemoResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkerMemo;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkerMemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerMemoService {

    private final WorkerMemoRepository workerMemoRepository;
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
    public WorkerMemoResponseDto saveMemo(
            Long workerId, WorkerMemoRequestDto requestDto, User loginUser) {
        validateCompanyOrManager(loginUser);

        User companyUser = getCompanyUser(loginUser);
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WorkerMemo memo = workerMemoRepository
                .findByCompanyAndWorker(companyUser, worker)
                .orElse(new WorkerMemo());

        memo.setCompany(companyUser);
        memo.setWorker(worker);
        memo.setMemo(requestDto.getMemo());

        return new WorkerMemoResponseDto(workerMemoRepository.save(memo));
    }

    @Transactional(readOnly = true)
    public List<WorkerMemoResponseDto> getMemos(User loginUser) {
        validateCompanyOrManager(loginUser);

        User companyUser = getCompanyUser(loginUser);
        return workerMemoRepository.findByCompany(companyUser)
                .stream()
                .map(WorkerMemoResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMemo(Long workerId, User loginUser) {
        validateCompanyOrManager(loginUser);

        User companyUser = getCompanyUser(loginUser);
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WorkerMemo memo = workerMemoRepository
                .findByCompanyAndWorker(companyUser, worker)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMO_NOT_FOUND));

        workerMemoRepository.delete(memo);
    }
}
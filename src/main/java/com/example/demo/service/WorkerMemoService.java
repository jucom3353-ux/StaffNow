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

    // 메모 저장 (없으면 생성, 있으면 수정)
    @Transactional
    public WorkerMemoResponseDto saveMemo(
            Long workerId, WorkerMemoRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WorkerMemo memo = workerMemoRepository
                .findByCompanyAndWorker(loginUser, worker)
                .orElse(new WorkerMemo());

        memo.setCompany(loginUser);
        memo.setWorker(worker);
        memo.setMemo(requestDto.getMemo());

        return new WorkerMemoResponseDto(workerMemoRepository.save(memo));
    }

    // 메모 목록 조회
    @Transactional(readOnly = true)
    public List<WorkerMemoResponseDto> getMemos(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        return workerMemoRepository.findByCompany(loginUser)
                .stream()
                .map(WorkerMemoResponseDto::new)
                .collect(Collectors.toList());
    }

    // 메모 삭제
    @Transactional
    public void deleteMemo(Long workerId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WorkerMemo memo = workerMemoRepository
                .findByCompanyAndWorker(loginUser, worker)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMO_NOT_FOUND));

        workerMemoRepository.delete(memo);
    }
}
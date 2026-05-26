package com.example.demo.service;

import com.example.demo.dto.WorkerScrapResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.entity.WorkerScrap;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkerScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkerScrapService {

    private final WorkerScrapRepository workerScrapRepository;
    private final UserRepository userRepository;

    // 스크랩 추가
    @Transactional
    public void addScrap(Long workerId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (workerScrapRepository.existsByCompanyAndWorker(loginUser, worker)) {
            throw new CustomException(ErrorCode.ALREADY_SCRAPPED);
        }

        WorkerScrap scrap = new WorkerScrap();
        scrap.setCompany(loginUser);
        scrap.setWorker(worker);
        workerScrapRepository.save(scrap);
    }

    // 스크랩 취소
    @Transactional
    public void removeScrap(Long workerId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        WorkerScrap scrap = workerScrapRepository
                .findByCompanyAndWorker(loginUser, worker)
                .orElseThrow(() -> new CustomException(ErrorCode.SCRAP_NOT_FOUND));

        workerScrapRepository.delete(scrap);
    }

    // 스크랩 목록 조회
    @Transactional(readOnly = true)
    public List<WorkerScrapResponseDto> getScraps(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        return workerScrapRepository.findByCompany(loginUser)
                .stream()
                .map(WorkerScrapResponseDto::new)
                .collect(Collectors.toList());
    }
}
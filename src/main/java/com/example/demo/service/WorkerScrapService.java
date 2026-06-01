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
    private final InvitationService invitationService;  // 추가

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
    public void addScrap(Long workerId, User loginUser) {
        validateCompanyOrManager(loginUser);
        User companyUser = getCompanyUser(loginUser);
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (workerScrapRepository.existsByCompanyAndWorker(companyUser, worker)) {
            throw new CustomException(ErrorCode.ALREADY_SCRAPPED);
        }
        WorkerScrap scrap = new WorkerScrap();
        scrap.setCompany(companyUser);
        scrap.setWorker(worker);
        workerScrapRepository.save(scrap);
    }

    @Transactional
    public void removeScrap(Long workerId, User loginUser) {
        validateCompanyOrManager(loginUser);
        User companyUser = getCompanyUser(loginUser);
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        WorkerScrap scrap = workerScrapRepository
                .findByCompanyAndWorker(companyUser, worker)
                .orElseThrow(() -> new CustomException(ErrorCode.SCRAP_NOT_FOUND));
        workerScrapRepository.delete(scrap);
    }

    @Transactional(readOnly = true)
    public List<WorkerScrapResponseDto> getScraps(User loginUser) {
        validateCompanyOrManager(loginUser);
        User companyUser = getCompanyUser(loginUser);
        return workerScrapRepository.findByCompany(companyUser)
                .stream()
                .map(WorkerScrapResponseDto::new)
                .collect(Collectors.toList());
    }

    // 스크랩한 구직자에게 바로 초대 보내기
    @Transactional
    public void inviteScrapedWorker(Long workerId, Long jobPostId, User loginUser) {
        validateCompanyOrManager(loginUser);
        User companyUser = getCompanyUser(loginUser);

        // 스크랩 여부 확인
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!workerScrapRepository.existsByCompanyAndWorker(companyUser, worker)) {
            throw new CustomException(ErrorCode.SCRAP_NOT_FOUND);
        }

        // 초대 발송
        invitationService.sendInvitation(jobPostId, workerId, loginUser);
    }
}
package com.example.demo.service;

import com.example.demo.dto.WorkerScrapResponseDto;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.entity.WorkerScrap;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkerScrapRepository;
  import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.util.AuthorizationUtil;
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
    private final InvitationService invitationService;

    @Transactional
    public void addScrap(Long workerId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);
        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);
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
        AuthorizationUtil.validateCompanyOrManager(loginUser);
        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        WorkerScrap scrap = workerScrapRepository
                .findByCompanyAndWorker(companyUser, worker)
                .orElseThrow(() -> new CustomException(ErrorCode.SCRAP_NOT_FOUND));
        workerScrapRepository.delete(scrap);
    }

    @Transactional(readOnly = true)
    public List<WorkerScrapResponseDto> getScraps(User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);
        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);
        return workerScrapRepository.findByCompany(companyUser)
                .stream()
                .map(WorkerScrapResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void inviteScrapedWorker(Long workerId, Long jobPostId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);
        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!workerScrapRepository.existsByCompanyAndWorker(companyUser, worker)) {
            throw new CustomException(ErrorCode.SCRAP_NOT_FOUND);
        }

        invitationService.sendInvitation(jobPostId, workerId, loginUser);
    }
}
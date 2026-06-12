package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ContractCreateRequestDto;
import com.example.demo.dto.ContractResponseDto;
import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.entity.JobPost;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CompanyStampRepository;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.AuthorizationUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final CompanyStampRepository companyStampRepository;

    @Transactional
    public void createContract(ContractCreateRequestDto requestDto, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        JobPost jobPost = jobPostRepository.findById(requestDto.getJobPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.JOB_POST_NOT_FOUND));

        if (!AuthorizationUtil.isMyJobPost(jobPost, loginUser)) {
            throw new CustomException(ErrorCode.NOT_MY_JOB_POST);
        }

        User worker = userRepository.findById(requestDto.getWorkerId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (worker.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        Contract contract = new Contract();
        contract.setJobPost(jobPost);
        contract.setCompany(companyUser);
        contract.setWorker(worker);
        contract.setContractStartDate(requestDto.getContractStartDate());
        contract.setContractEndDate(requestDto.getContractEndDate());
        contract.setStatus(ContractStatus.PENDING);
        contractRepository.save(contract);

        notificationService.send(worker, NotificationType.CONTRACT_CREATED,
                "[" + jobPost.getTitle() + "] 근로계약서가 생성되었습니다. 서명해주세요.",
                contract.getId());
    }

    @Transactional(readOnly = true)
    public List<ContractResponseDto> getMyContracts(User loginUser) {
        if (loginUser.getRole() == Role.COMPANY || loginUser.getRole() == Role.MANAGER) {
            User companyUser = AuthorizationUtil.getCompanyUser(loginUser);
            return contractRepository.findByCompany(companyUser).stream()
                    .map(ContractResponseDto::new).collect(Collectors.toList());
        }
        return contractRepository.findByWorker(loginUser).stream()
                .map(ContractResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractResponseDto getContract(Long contractId, User loginUser) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        if (!contract.getCompany().getId().equals(companyUser.getId()) &&
            !contract.getWorker().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_CONTRACT);
        }

        return new ContractResponseDto(contract);
    }

    @Transactional
    public void signContract(Long contractId, String signatureUrl, User loginUser) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_CANCELLED);
        }
        if (contract.getStatus() == ContractStatus.SIGNED) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
        }
        if (contract.getStatus() == ContractStatus.EXPIRED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "만료된 계약서입니다. (1개월 내 미서명)");
        }
        if (contract.getStatus() == ContractStatus.DOWNLOAD_EXPIRED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "다운로드 기간이 만료된 계약서입니다.");
        }

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        if (companyUser.getId().equals(contract.getCompany().getId())) {
            if (contract.getCompanySignedAt() != null) {
                throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
            }

            if (signatureUrl != null && !signatureUrl.isBlank()) {
                contract.setCompanySignatureUrl(signatureUrl);
            } else {
                companyStampRepository.findByUser(companyUser)
                        .ifPresent(stamp ->
                                contract.setCompanySignatureUrl(stamp.getStampUrl()));
            }

            contract.setCompanySignedAt(LocalDateTime.now());
            notificationService.send(contract.getWorker(), NotificationType.CONTRACT_SIGNED,
                    "[" + contract.getJobPost().getTitle() + "] 기업이 계약서에 서명했습니다. 서명해주세요.",
                    contract.getId());

        } else if (loginUser.getId().equals(contract.getWorker().getId())) {
            if (contract.getWorkerSignedAt() != null) {
                throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
            }

            if (signatureUrl != null && !signatureUrl.isBlank()) {
                contract.setWorkerSignatureUrl(signatureUrl);
            }

            contract.setWorkerSignedAt(LocalDateTime.now());
            notificationService.send(contract.getCompany(), NotificationType.CONTRACT_SIGNED,
                    "[" + contract.getJobPost().getTitle() + "] 근로자가 계약서에 서명했습니다.",
                    contract.getId());
        } else {
            throw new CustomException(ErrorCode.NOT_MY_CONTRACT);
        }

        if (contract.getCompanySignedAt() != null && contract.getWorkerSignedAt() != null) {
            contract.setStatus(ContractStatus.SIGNED);
            notificationService.send(contract.getCompany(), NotificationType.CONTRACT_COMPLETED,
                    "[" + contract.getJobPost().getTitle() + "] 계약이 체결되었습니다.",
                    contract.getId());
            notificationService.send(contract.getWorker(), NotificationType.CONTRACT_COMPLETED,
                    "[" + contract.getJobPost().getTitle() + "] 계약이 체결되었습니다.",
                    contract.getId());
        }

        contractRepository.save(contract);
    }

    @Transactional
    public void cancelContract(Long contractId, User loginUser) {
        AuthorizationUtil.validateCompanyOrManager(loginUser);

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        if (!contract.getCompany().getId().equals(companyUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_CONTRACT);
        }
        if (contract.getStatus() == ContractStatus.SIGNED) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_SIGNED);
        }

        contract.setStatus(ContractStatus.CANCELLED);
        contractRepository.save(contract);

        notificationService.send(contract.getWorker(), NotificationType.CONTRACT_CANCELLED,
                "[" + contract.getJobPost().getTitle() + "] 계약서가 취소되었습니다.",
                contract.getId());
    }

    @Transactional(readOnly = true)
    public void validateDownload(Long contractId, User loginUser) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        User companyUser = AuthorizationUtil.getCompanyUser(loginUser);

        if (!contract.getCompany().getId().equals(companyUser.getId()) &&
            !contract.getWorker().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.NOT_MY_CONTRACT);
        }

        if (contract.getStatus() == ContractStatus.DOWNLOAD_EXPIRED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "다운로드 기간이 만료된 계약서입니다. (완료 후 1년)");
        }
        if (contract.getStatus() == ContractStatus.EXPIRED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "만료된 계약서입니다. (1개월 내 미서명)");
        }
        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new CustomException(ErrorCode.CONTRACT_ALREADY_CANCELLED);
        }
    }
}
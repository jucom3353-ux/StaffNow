package com.example.demo.service;

import com.example.demo.dto.ContractCreateRequestDto;
import com.example.demo.dto.ContractResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.ContractRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final JobPostRepository jobPostRepository;
    private final UserRepository userRepository;

    // 계약서 생성 (기업만 가능)
    @Transactional
    public void createContract(ContractCreateRequestDto requestDto, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 계약서를 생성할 수 있습니다.");
        }

        JobPost jobPost = jobPostRepository.findById(requestDto.getJobPostId())
                .orElseThrow(() -> new RuntimeException("공고 없음"));

        if (!jobPost.getUser().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 공고에만 계약서를 생성할 수 있습니다.");
        }

        User worker = userRepository.findById(requestDto.getWorkerId())
                .orElseThrow(() -> new RuntimeException("근로자 없음"));

        if (worker.getRole() != Role.INDIVIDUAL) {
            throw new RuntimeException("구직자만 계약 대상이 될 수 있습니다.");
        }

        Contract contract = new Contract();
        contract.setJobPost(jobPost);
        contract.setCompany(loginUser);
        contract.setWorker(worker);
        contract.setContractStartDate(requestDto.getContractStartDate());
        contract.setContractEndDate(requestDto.getContractEndDate());
        contract.setStatus(ContractStatus.PENDING);

        contractRepository.save(contract);
    }

    // 내 계약서 목록 조회 (기업/구직자 둘 다)
    @Transactional(readOnly = true)
    public List<ContractResponseDto> getMyContracts(User loginUser) {

        if (loginUser.getRole() == Role.COMPANY) {
            return contractRepository.findByCompany(loginUser).stream()
                    .map(ContractResponseDto::new)
                    .collect(Collectors.toList());
        }

        return contractRepository.findByWorker(loginUser).stream()
                .map(ContractResponseDto::new)
                .collect(Collectors.toList());
    }

    // 계약서 단건 조회
    @Transactional(readOnly = true)
    public ContractResponseDto getContract(Long contractId, User loginUser) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("계약서 없음"));

        if (!contract.getCompany().getId().equals(loginUser.getId()) &&
            !contract.getWorker().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 계약서만 조회 가능합니다.");
        }

        return new ContractResponseDto(contract);
    }

    // 서명 (기업/구직자 각각)
    @Transactional
    public void signContract(Long contractId, User loginUser) {

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("계약서 없음"));

        if (contract.getStatus() == ContractStatus.CANCELLED) {
            throw new RuntimeException("취소된 계약서입니다.");
        }

        if (contract.getStatus() == ContractStatus.SIGNED) {
            throw new RuntimeException("이미 서명 완료된 계약서입니다.");
        }

        // 기업 서명
        if (loginUser.getId().equals(contract.getCompany().getId())) {
            if (contract.getCompanySignedAt() != null) {
                throw new RuntimeException("이미 서명하셨습니다.");
            }
            contract.setCompanySignedAt(LocalDateTime.now());
        }
        // 근로자 서명
        else if (loginUser.getId().equals(contract.getWorker().getId())) {
            if (contract.getWorkerSignedAt() != null) {
                throw new RuntimeException("이미 서명하셨습니다.");
            }
            contract.setWorkerSignedAt(LocalDateTime.now());
        } else {
            throw new RuntimeException("본인 계약서만 서명 가능합니다.");
        }

        // 양쪽 다 서명하면 SIGNED 상태로 변경
        if (contract.getCompanySignedAt() != null &&
            contract.getWorkerSignedAt() != null) {
            contract.setStatus(ContractStatus.SIGNED);
        }

        contractRepository.save(contract);
    }

    // 계약서 취소 (기업만)
    @Transactional
    public void cancelContract(Long contractId, User loginUser) {

        if (loginUser.getRole() != Role.COMPANY) {
            throw new RuntimeException("기업 회원만 계약서를 취소할 수 있습니다.");
        }

        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("계약서 없음"));

        if (!contract.getCompany().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 계약서만 취소 가능합니다.");
        }

        if (contract.getStatus() == ContractStatus.SIGNED) {
            throw new RuntimeException("서명 완료된 계약서는 취소할 수 없습니다.");
        }

        contract.setStatus(ContractStatus.CANCELLED);
        contractRepository.save(contract);
    }
}
package com.example.demo.scheduler;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.entity.NotificationType;
import com.example.demo.repository.ContractRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractScheduler {

    private final ContractRepository contractRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireContracts() {

        // 1. PENDING 1개월 초과 → EXPIRED
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Contract> unsignedContracts =
                contractRepository.findUnsignedExpiredContracts(oneMonthAgo);

        for (Contract contract : unsignedContracts) {
            contract.setStatus(ContractStatus.EXPIRED);
            contractRepository.save(contract);

            notificationService.send(
                    contract.getCompany(),
                    NotificationType.CONTRACT_CANCELLED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    contract.getWorker().getName() +
                    "님과의 근로계약서가 1개월 내 미서명으로 만료되었습니다.",
                    contract.getId()
            );
            notificationService.send(
                    contract.getWorker(),
                    NotificationType.CONTRACT_CANCELLED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    "근로계약서가 1개월 내 미서명으로 만료되었습니다.",
                    contract.getId()
            );

            log.info("계약서 만료 처리: contractId={}", contract.getId());
        }

        // 2. SIGNED 1년 초과 → DOWNLOAD_EXPIRED
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<Contract> expiredDownloadContracts =
                contractRepository.findDownloadExpiredContracts(oneYearAgo); // ✅ 메서드명 수정

        for (Contract contract : expiredDownloadContracts) {
            contract.setStatus(ContractStatus.DOWNLOAD_EXPIRED);
            contractRepository.save(contract);

            notificationService.send(
                    contract.getCompany(),
                    NotificationType.CONTRACT_CANCELLED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    contract.getWorker().getName() +
                    "님과의 근로계약서 다운로드 기간이 만료되었습니다.",
                    contract.getId()
            );
            notificationService.send(
                    contract.getWorker(),
                    NotificationType.CONTRACT_CANCELLED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    "근로계약서 다운로드 기간이 만료되었습니다.",
                    contract.getId()
            );

            log.info("계약서 다운로드 만료 처리: contractId={}", contract.getId());
        }
    }
}
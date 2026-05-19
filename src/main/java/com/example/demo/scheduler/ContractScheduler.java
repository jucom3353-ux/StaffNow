package com.example.demo.scheduler;

import com.example.demo.entity.Contract;
import com.example.demo.entity.ContractStatus;
import com.example.demo.entity.NotificationType;
import com.example.demo.repository.ContractRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContractScheduler {

    private final ContractRepository contractRepository;
    private final NotificationService notificationService;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireContracts() {

        // 1. 기존: 1개월 지난 PENDING 계약서 → EXPIRED
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Contract> unsignedContracts =
                contractRepository.findUnsignedExpiredContracts(oneMonthAgo);

        for (Contract contract : unsignedContracts) {
            contract.setStatus(ContractStatus.EXPIRED);
            contractRepository.save(contract);

            // 기업에게 알림
            notificationService.send(
                    contract.getCompany(),
                    NotificationType.CONTRACT_CREATED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    contract.getWorker().getName() +
                    "님과의 근로계약서가 1개월 내 미서명으로 만료 처리되었습니다.",
                    contract.getId()
            );

            // 근로자에게 알림
            notificationService.send(
                    contract.getWorker(),
                    NotificationType.CONTRACT_CREATED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    "근로계약서가 1개월 내 미서명으로 만료 처리되었습니다.",
                    contract.getId()
            );
        }

        // 2. 신규: 완료 후 1년 지난 계약서 → DOWNLOAD_EXPIRED
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<Contract> expiredDownloadContracts =
                contractRepository.findExpiredCompletedContracts(oneYearAgo);

        for (Contract contract : expiredDownloadContracts) {
            contract.setStatus(ContractStatus.DOWNLOAD_EXPIRED);
            contractRepository.save(contract);

            // 기업에게 알림
            notificationService.send(
                    contract.getCompany(),
                    NotificationType.CONTRACT_CREATED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    contract.getWorker().getName() +
                    "님과의 근로계약서 다운로드 기간이 만료되었습니다.",
                    contract.getId()
            );

            // 근로자에게 알림
            notificationService.send(
                    contract.getWorker(),
                    NotificationType.CONTRACT_CREATED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    "근로계약서 다운로드 기간이 만료되었습니다.",
                    contract.getId()
            );
        }
    }
}
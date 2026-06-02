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

    // 매일 자정 - 계약서 만료 처리
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireContracts() {

        // 1. PENDING 1개월 초과 → EXPIRED
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Contract> unsignedContracts =
                contractRepository.findUnsignedExpiredContracts(oneMonthAgo);

        for (Contract contract : unsignedContracts) {
            try {
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

            } catch (Exception e) {
                log.error("계약서 만료 처리 실패: contractId={}, error={}",
                        contract.getId(), e.getMessage());
            }
        }

        // 2. SIGNED 1년 초과 → DOWNLOAD_EXPIRED
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        List<Contract> expiredDownloadContracts =
                contractRepository.findDownloadExpiredContracts(oneYearAgo);

        for (Contract contract : expiredDownloadContracts) {
            try {
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

            } catch (Exception e) {
                log.error("계약서 다운로드 만료 처리 실패: contractId={}, error={}",
                        contract.getId(), e.getMessage());
            }
        }
    }

    // 매일 오전 8시 - 미서명 계약서 3일차/7일차 리마인드
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void sendSignReminderNotification() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 3일 미서명
        List<Contract> threeDayUnsigned =
                contractRepository.findPendingContractsCreatedAt(
                        threeDaysAgo.minusHours(1), threeDaysAgo.plusHours(1));

        for (Contract contract : threeDayUnsigned) {
            try {
                sendSignReminder(contract, "3일");
            } catch (Exception e) {
                log.error("서명 리마인드(3일) 실패: contractId={}, error={}",
                        contract.getId(), e.getMessage());
            }
        }

        // 7일 미서명
        List<Contract> sevenDayUnsigned =
                contractRepository.findPendingContractsCreatedAt(
                        sevenDaysAgo.minusHours(1), sevenDaysAgo.plusHours(1));

        for (Contract contract : sevenDayUnsigned) {
            try {
                sendSignReminder(contract, "7일");
            } catch (Exception e) {
                log.error("서명 리마인드(7일) 실패: contractId={}, error={}",
                        contract.getId(), e.getMessage());
            }
        }
    }

    private void sendSignReminder(Contract contract, String dayLabel) {
        // 기업 미서명
        if (contract.getCompanySignedAt() == null) {
            notificationService.send(
                    contract.getCompany(),
                    NotificationType.CONTRACT_CREATED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    "근로계약서 서명 후 " + dayLabel +
                    "이 지났습니다. 서명을 완료해주세요.",
                    contract.getId()
            );
        }

        // 근로자 미서명
        if (contract.getWorkerSignedAt() == null) {
            notificationService.send(
                    contract.getWorker(),
                    NotificationType.CONTRACT_CREATED,
                    "[" + contract.getJobPost().getTitle() + "] " +
                    "근로계약서 서명 후 " + dayLabel +
                    "이 지났습니다. 서명을 완료해주세요.",
                    contract.getId()
            );
        }

        log.info("서명 리마인드({}) 발송: contractId={}", dayLabel, contract.getId());
    }
}
package com.example.demo.scheduler;

import com.example.demo.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InvitationScheduler {

    private final InvitationRepository invitationRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireInvitations() {
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(3);
        invitationRepository.expireOldInvitations(expiredTime);
    }
}
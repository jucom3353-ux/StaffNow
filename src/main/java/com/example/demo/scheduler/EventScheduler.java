package com.example.demo.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Event;
import com.example.demo.entity.EventStatus;
import com.example.demo.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;

    // 매일 자정 종료일 지난 이벤트 자동 ENDED 처리
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireEvents() {
        String today = LocalDate.now().toString();

        List<Event> expiredEvents = eventRepository.findExpiredOngoingEvents(today);

        if (expiredEvents.isEmpty()) return;

        expiredEvents.forEach(e -> e.setStatus(EventStatus.ENDED));
        eventRepository.saveAll(expiredEvents);

        log.info("[EventScheduler] 이벤트 자동 종료 처리: {}건", expiredEvents.size());
    }
}
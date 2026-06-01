package com.example.demo.scheduler;

import com.example.demo.entity.*;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookmarkScheduler {

    private final BookmarkRepository bookmarkRepository;
    private final NotificationService notificationService;

    // 매일 오전 9시 마감 D-3 알림
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void notifyDeadlineSoon() {
        String threeDaysLater = LocalDate.now().plusDays(3).toString();

        List<Bookmark> bookmarks = bookmarkRepository.findAll();

        bookmarks.stream()
                .filter(b -> b.getJobPost().getPostStatus() == PostStatus.OPEN)
                .filter(b -> b.getJobPost().getDeadline() != null)
                .filter(b -> b.getJobPost().getDeadline().equals(threeDaysLater))
                .forEach(b -> notificationService.send(
                        b.getUser(),
                        NotificationType.BOOKMARK_DEADLINE_SOON,
                        "[마감 D-3] " + b.getJobPost().getTitle() + " 공고가 3일 후 마감됩니다.",
                        b.getJobPost().getId()
                ));

        log.info("[BookmarkScheduler] 마감 임박 알림 처리 완료");
    }
}
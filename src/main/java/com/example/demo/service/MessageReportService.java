package com.example.demo.service;

import com.example.demo.dto.MessageReportRequestDto;
import com.example.demo.dto.MessageReportResponseDto;
import com.example.demo.entity.*;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.MessageReportRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageReportService {

    private final MessageReportRepository messageReportRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // 메시지 신고
    @Transactional
    public MessageReportResponseDto reportMessage(
            MessageReportRequestDto requestDto, User loginUser) {

        Message message = messageRepository.findById(requestDto.getMessageId())
                .orElseThrow(() -> new RuntimeException("메시지 없음"));

        if (message.getSender().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 메시지는 신고할 수 없습니다.");
        }

        if (messageReportRepository.existsByReporterAndMessage(loginUser, message)) {
            throw new RuntimeException("이미 신고한 메시지입니다.");
        }

        if (requestDto.getReason() == null) {
            throw new RuntimeException("신고 사유를 선택해주세요.");
        }

        MessageReport report = new MessageReport();
        report.setReporter(loginUser);
        report.setMessage(message);
        report.setReason(requestDto.getReason());
        report.setDescription(requestDto.getDescription());

        return new MessageReportResponseDto(messageReportRepository.save(report));
    }

    // 내 신고 목록 조회
    @Transactional(readOnly = true)
    public List<MessageReportResponseDto> getMyReports(User loginUser) {
        return messageReportRepository.findByReporter(loginUser)
                .stream()
                .map(MessageReportResponseDto::new)
                .collect(Collectors.toList());
    }

    // ===== ADMIN 전용 =====

    // 전체 신고 목록 조회
    @Transactional(readOnly = true)
    public List<MessageReportResponseDto> getAllReports(
            MessageReportStatus status, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 조회 가능합니다.");
        }

        List<MessageReport> reports = status != null
                ? messageReportRepository.findByStatus(status)
                : messageReportRepository.findAll();

        return reports.stream()
                .map(MessageReportResponseDto::new)
                .collect(Collectors.toList());
    }

    // 신고 처리 (승인 - 발송자 경고)
    @Transactional
    public MessageReportResponseDto approveReport(Long reportId, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 처리 가능합니다.");
        }

        MessageReport report = messageReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 없음"));

        if (report.getStatus() != MessageReportStatus.PENDING) {
            throw new RuntimeException("대기 상태의 신고만 처리 가능합니다.");
        }

        report.setStatus(MessageReportStatus.APPROVED);

        // 발송자 노쇼카운트 증가 (경고 누적)
        User sender = report.getMessage().getSender();
        sender.setNoShowCount(sender.getNoShowCount() + 1);
        userRepository.save(sender);

        // 3회 이상 경고 시 자동 정지
        if (sender.getNoShowCount() >= 3) {
            sender.setSuspended(true);
            userRepository.save(sender);
        }

        return new MessageReportResponseDto(messageReportRepository.save(report));
    }

    // 신고 기각
    @Transactional
    public MessageReportResponseDto dismissReport(Long reportId, User loginUser) {

        if (loginUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자만 처리 가능합니다.");
        }

        MessageReport report = messageReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고 없음"));

        if (report.getStatus() != MessageReportStatus.PENDING) {
            throw new RuntimeException("대기 상태의 신고만 처리 가능합니다.");
        }

        report.setStatus(MessageReportStatus.DISMISSED);

        return new MessageReportResponseDto(messageReportRepository.save(report));
    }
}
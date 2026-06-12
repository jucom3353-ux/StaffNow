package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.MessageReportRequestDto;
import com.example.demo.dto.MessageReportResponseDto;
import com.example.demo.entity.Message;
import com.example.demo.entity.MessageReport;
import com.example.demo.entity.MessageReportStatus;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.MessageReportRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageReportService {

    private final MessageReportRepository messageReportRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public MessageReportResponseDto reportMessage(
            MessageReportRequestDto requestDto, User loginUser) {

        Message message = messageRepository.findById(requestDto.getMessageId())
                .orElseThrow(() -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getSender().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }

        if (messageReportRepository.existsByReporterAndMessage(loginUser, message)) {
            throw new CustomException(ErrorCode.ALREADY_REPORTED);
        }

        if (requestDto.getReason() == null) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REASON);
        }

        MessageReport report = new MessageReport();
        report.setReporter(loginUser);
        report.setMessage(message);
        report.setReason(requestDto.getReason());
        report.setDescription(requestDto.getDescription());

        return new MessageReportResponseDto(messageReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public List<MessageReportResponseDto> getMyReports(User loginUser) {
        return messageReportRepository.findByReporter(loginUser).stream()
                .map(MessageReportResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageReportResponseDto> getAllReports(
            MessageReportStatus status, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        List<MessageReport> reports = status != null
                ? messageReportRepository.findByStatus(status)
                : messageReportRepository.findAll();

        return reports.stream().map(MessageReportResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public MessageReportResponseDto approveReport(Long reportId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        MessageReport report = messageReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() != MessageReportStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 상태의 신고만 처리 가능합니다.");
        }

        report.setStatus(MessageReportStatus.APPROVED);

        User sender = report.getMessage().getSender();
        sender.setNoShowCount(sender.getNoShowCount() + 1);
        userRepository.save(sender);

        if (sender.getNoShowCount() >= 3) {
            sender.setSuspended(true);
            userRepository.save(sender);
        }

        MessageReportResponseDto result =
                new MessageReportResponseDto(messageReportRepository.save(report));

        notificationService.send(
                report.getReporter(),
                NotificationType.REPORT_APPROVED,
                "신고하신 메시지가 검토 완료되었습니다.",
                report.getId()
        );

        return result;
    }

    @Transactional
    public MessageReportResponseDto dismissReport(Long reportId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }

        MessageReport report = messageReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() != MessageReportStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "대기 상태의 신고만 처리 가능합니다.");
        }

        report.setStatus(MessageReportStatus.DISMISSED);

        MessageReportResponseDto result =
                new MessageReportResponseDto(messageReportRepository.save(report));

        notificationService.send(
                report.getReporter(),
                NotificationType.REPORT_DISMISSED,
                "신고하신 메시지가 검토 결과 기각되었습니다.",
                report.getId()
        );

        return result;
    }
}
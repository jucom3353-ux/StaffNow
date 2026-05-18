package com.example.demo.service;

import com.example.demo.dto.MessageReportRequestDto;
import com.example.demo.dto.MessageReportResponseDto;
import com.example.demo.entity.Message;
import com.example.demo.entity.MessageReport;
import com.example.demo.entity.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.MessageReportRepository;
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

    // 메시지 신고
    @Transactional
    public MessageReportResponseDto reportMessage(
            MessageReportRequestDto requestDto, User loginUser) {

        Message message = messageRepository.findById(requestDto.getMessageId())
                .orElseThrow(() -> new RuntimeException("메시지 없음"));

        // 본인 메시지 신고 불가
        if (message.getSender().getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인 메시지는 신고할 수 없습니다.");
        }

        // 중복 신고 방지
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
}
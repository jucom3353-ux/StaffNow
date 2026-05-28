package com.example.demo.service;

import com.example.demo.dto.WorkSessionQrResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkSessionQrService {

    private final WorkSessionQrRepository workSessionQrRepository;
    private final WorkSessionRepository workSessionRepository;
    private final ApplicationRepository applicationRepository;
    private final WorkAttendanceRepository workAttendanceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String fileBaseUrl;

    private static final int QR_SIZE = 300;

    // QR 생성 (기업/MANAGER)
    @Transactional
    public WorkSessionQrResponseDto generateQr(Long workSessionId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY && loginUser.getRole() != Role.MANAGER) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        WorkSession workSession = workSessionRepository.findById(workSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORK_SESSION_NOT_FOUND));

        // 이미 QR 있으면 기존 반환
        return workSessionQrRepository.findByWorkSession(workSession)
                .map(WorkSessionQrResponseDto::new)
                .orElseGet(() -> {
                    String token = UUID.randomUUID().toString();
                    String qrImageUrl = generateQrImage(token, workSessionId);

                    WorkSessionQr qr = new WorkSessionQr();
                    qr.setWorkSession(workSession);
                    qr.setQrToken(token);
                    qr.setQrImageUrl(qrImageUrl);
                    qr.setActive(true);

                    return new WorkSessionQrResponseDto(workSessionQrRepository.save(qr));
                });
    }

    // QR 스캔 → 출퇴근 처리
    @Transactional
    public String scanQr(String token, User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }

        WorkSessionQr qr = workSessionQrRepository.findByQrToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.QR_NOT_FOUND));

        if (!qr.isActive()) {
            throw new CustomException(ErrorCode.QR_EXPIRED);
        }

        WorkSession workSession = qr.getWorkSession();

        // 출근 시간 검증 (시작 30분 전부터 가능)
        LocalTime now = LocalTime.now();
        if (workSession.getStartTime() != null) {
            LocalTime startTime = LocalTime.parse(
                    workSession.getStartTime(),
                    DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime allowedFrom = startTime.minusMinutes(30);
            if (now.isBefore(allowedFrom)) {
                throw new CustomException(ErrorCode.QR_TOO_EARLY);
            }
        }

        // 해당 WorkSession의 내 Application 찾기
        Application application = applicationRepository
                .findByUserAndWorkSession(loginUser, workSession)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // 출퇴근 처리
        return workAttendanceRepository.findByApplication(application)
                .map(attendance -> {
                    // 이미 출근 → 퇴근 처리
                    if (attendance.getCheckOutTime() != null) {
                        throw new CustomException(ErrorCode.ALREADY_CHECKED_OUT);
                    }

                    // 퇴근 시간 검증 (종료 후 1시간 이내)
                    if (workSession.getEndTime() != null) {
                        LocalTime endTime = LocalTime.parse(
                                workSession.getEndTime(),
                                DateTimeFormatter.ofPattern("HH:mm"));
                        LocalTime deadline = endTime.plusHours(1);
                        if (now.isAfter(deadline)) {
                            throw new CustomException(ErrorCode.QR_CHECKOUT_EXPIRED);
                        }
                    }

                    attendance.setCheckOutTime(java.time.LocalDateTime.now());
                    workAttendanceRepository.save(attendance);

                    notificationService.send(
                            loginUser,
                            NotificationType.ATTENDANCE_CHECKED_OUT,
                            "[" + application.getJobPost().getTitle() + "] QR 퇴근이 확인되었습니다.",
                            attendance.getId()
                    );
                    return "퇴근 처리 완료";
                })
                .orElseGet(() -> {
                    // 출근 처리
                    WorkAttendance attendance = new WorkAttendance();
                    attendance.setApplication(application);
                    attendance.setWorkSession(workSession);
                    attendance.setCheckInTime(java.time.LocalDateTime.now());

                    // 지각 여부 판단
                    AttendanceStatus status = AttendanceStatus.NORMAL;
                    if (workSession.getStartTime() != null) {
                        LocalTime startTime = LocalTime.parse(
                                workSession.getStartTime(),
                                DateTimeFormatter.ofPattern("HH:mm"));
                        if (now.isAfter(startTime)) {
                            status = AttendanceStatus.LATE;
                        }
                    }
                    attendance.setStatus(status);
                    workAttendanceRepository.save(attendance);

                    String message = status == AttendanceStatus.LATE
                            ? "[" + application.getJobPost().getTitle() + "] QR 출근 - 지각 처리되었습니다."
                            : "[" + application.getJobPost().getTitle() + "] QR 출근이 확인되었습니다.";

                    notificationService.send(
                            loginUser,
                            status == AttendanceStatus.LATE
                                    ? NotificationType.ATTENDANCE_LATE
                                    : NotificationType.ATTENDANCE_CHECKED_IN,
                            message,
                            attendance.getId()
                    );
                    return "출근 처리 완료";
                });
    }

    // QR 이미지 생성
    private String generateQrImage(String token, Long workSessionId) {
        try {
            String dirPath = System.getProperty("user.dir") + "/" + uploadDir + "/qr";
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = "qr_" + workSessionId + "_" + token + ".png";
            String filePath = dirPath + "/" + fileName;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    token, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);

            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            return fileBaseUrl + "/uploads/qr/" + fileName;
        } catch (Exception e) {
            log.error("QR 이미지 생성 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
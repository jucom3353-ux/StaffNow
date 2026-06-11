package com.example.demo.service;

import com.example.demo.entity.AuditLog;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void log(User admin, String action, String targetType,
                    Long targetId, String detail, String ipAddress) {
        AuditLog log = new AuditLog();
        log.setAdminId(admin.getId());
        log.setAdminEmail(admin.getEmail());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getLogs(User loginUser, Pageable pageable) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
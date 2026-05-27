package com.example.demo.service;

import com.example.demo.entity.CompanyInviteCode;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CompanyInviteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyInviteService {

    private final CompanyInviteCodeRepository companyInviteCodeRepository;
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    @Transactional
    public String generateInviteCode(User loginUser, Role role) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        do {
            code.setLength(0);
            for (int i = 0; i < CODE_LENGTH; i++) {
                code.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
        } while (companyInviteCodeRepository.existsByCode(code.toString()));

        CompanyInviteCode inviteCode = new CompanyInviteCode();
        inviteCode.setCompany(loginUser);
        inviteCode.setCode(code.toString());
        inviteCode.setRole(role != null ? role : Role.MANAGER);
        inviteCode.setExpiredAt(LocalDateTime.now().plusDays(7));
        companyInviteCodeRepository.save(inviteCode);

        return code.toString();
    }

    @Transactional
    public CompanyInviteCode validateInviteCode(String code) {
        CompanyInviteCode inviteCode = companyInviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFERRAL_CODE));

        if (inviteCode.isUsed()) {
            throw new CustomException(ErrorCode.ALREADY_ASSIGNED);
        }
        if (inviteCode.getExpiredAt() != null &&
                inviteCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.VERIFY_CODE_EXPIRED);
        }

        inviteCode.setUsed(true);
        companyInviteCodeRepository.save(inviteCode);
        return inviteCode;
    }

    @Transactional(readOnly = true)
    public List<CompanyInviteCode> getInviteCodes(User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        return companyInviteCodeRepository.findByCompany(loginUser);
    }

    @Transactional
    public void cancelInviteCode(Long inviteCodeId, User loginUser) {
        if (loginUser.getRole() != Role.COMPANY) {
            throw new CustomException(ErrorCode.COMPANY_ONLY);
        }
        CompanyInviteCode inviteCode = companyInviteCodeRepository.findById(inviteCodeId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!inviteCode.getCompany().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        companyInviteCodeRepository.delete(inviteCode);
    }
}
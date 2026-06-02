package com.example.demo.service;

import com.example.demo.dto.MileageResponseDto;
import com.example.demo.dto.MileageWithdrawalResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.MileageRepository;
import com.example.demo.repository.MileageWithdrawalRepository;
import com.example.demo.repository.ProfileBoostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MileageService {

    private final MileageRepository mileageRepository;
    private final MileageWithdrawalRepository mileageWithdrawalRepository;
    private final UserRepository userRepository;
    private final ProfileBoostRepository profileBoostRepository;

    private static final int MIN_WITHDRAWAL = 30000;
    private static final double TAX_RATE = 0.033;
    private static final int BOOST_COST = 1000;

    @Transactional
    public void addMileage(User user, MileageType type, int amount,
                           String description, Long referenceId) {
        if (user.getRole() != Role.INDIVIDUAL) return;

        int newBalance = user.getMileage() + amount;
        if (newBalance < 0) newBalance = 0;

        user.setMileage(newBalance);
        userRepository.save(user);

        Mileage mileage = new Mileage();
        mileage.setUser(user);
        mileage.setType(type);
        mileage.setAmount(amount);
        mileage.setBalanceAfter(newBalance);
        mileage.setDescription(description);
        mileage.setReferenceId(referenceId);
        mileageRepository.save(mileage);
    }

    @Transactional(readOnly = true)
    public List<MileageResponseDto> getMyMileage(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }
        return mileageRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream().map(MileageResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    public void exchangeBoost(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }
        if (loginUser.getMileage() < BOOST_COST) {
            throw new CustomException(ErrorCode.MILEAGE_NOT_ENOUGH);
        }
        profileBoostRepository.findActiveBoost(loginUser, LocalDateTime.now())
                .ifPresent(b -> {
                    throw new CustomException(ErrorCode.BOOST_ALREADY_ACTIVE);
                });

        addMileage(loginUser, MileageType.BOOST_USED, -BOOST_COST,
                "프로필 부스트 1일권 교환", null);

        LocalDateTime now = LocalDateTime.now();
        ProfileBoost boost = new ProfileBoost();
        boost.setUser(loginUser);
        boost.setStartAt(now);
        boost.setEndAt(now.plusDays(1));
        boost.setActive(true);
        profileBoostRepository.save(boost);
    }

    @Transactional
    public MileageWithdrawalResponseDto requestWithdrawal(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }
        if (loginUser.getMileage() < MIN_WITHDRAWAL) {
            throw new CustomException(ErrorCode.MILEAGE_NOT_ENOUGH);
        }
        if (mileageWithdrawalRepository.existsByUserAndStatus(
                loginUser, MileageWithdrawalStatus.PENDING)) {
            throw new CustomException(ErrorCode.WITHDRAWAL_ALREADY_PENDING);
        }
        if (loginUser.getBankName() == null || loginUser.getAccountNumber() == null) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_REGISTERED);
        }

        int requestAmount = loginUser.getMileage();
        int taxDeduction = (int) (requestAmount * TAX_RATE);
        int netAmount = requestAmount - taxDeduction;

        addMileage(loginUser, MileageType.WITHDRAWAL_REQUESTED,
                -requestAmount, "출금 신청", null);

        MileageWithdrawal withdrawal = new MileageWithdrawal();
        withdrawal.setUser(loginUser);
        withdrawal.setRequestAmount(requestAmount);
        withdrawal.setTaxDeduction(taxDeduction);
        withdrawal.setNetAmount(netAmount);
        withdrawal.setBankName(loginUser.getBankName());
        withdrawal.setAccountNumber(loginUser.getAccountNumber());
        withdrawal.setAccountHolder(loginUser.getAccountHolder());
        withdrawal.setStatus(MileageWithdrawalStatus.PENDING);

        return new MileageWithdrawalResponseDto(
                mileageWithdrawalRepository.save(withdrawal));
    }

    @Transactional
    public void cancelWithdrawal(Long withdrawalId, User loginUser) {
        MileageWithdrawal withdrawal = mileageWithdrawalRepository
                .findById(withdrawalId)
                .orElseThrow(() -> new CustomException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (!withdrawal.getUser().getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        if (withdrawal.getStatus() != MileageWithdrawalStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        withdrawal.setStatus(MileageWithdrawalStatus.CANCELLED);
        mileageWithdrawalRepository.save(withdrawal);

        addMileage(loginUser, MileageType.WITHDRAWAL_CANCELLED,
                withdrawal.getRequestAmount(), "출금 취소 환불", withdrawalId);
    }

    @Transactional
    public void approveWithdrawal(Long withdrawalId, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        MileageWithdrawal withdrawal = mileageWithdrawalRepository
                .findById(withdrawalId)
                .orElseThrow(() -> new CustomException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (withdrawal.getStatus() != MileageWithdrawalStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        withdrawal.setStatus(MileageWithdrawalStatus.APPROVED);
        mileageWithdrawalRepository.save(withdrawal);
    }

    @Transactional
    public void rejectWithdrawal(Long withdrawalId, String rejectReason, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        MileageWithdrawal withdrawal = mileageWithdrawalRepository
                .findById(withdrawalId)
                .orElseThrow(() -> new CustomException(ErrorCode.WITHDRAWAL_NOT_FOUND));

        if (withdrawal.getStatus() != MileageWithdrawalStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        withdrawal.setStatus(MileageWithdrawalStatus.REJECTED);
        withdrawal.setRejectReason(rejectReason);
        mileageWithdrawalRepository.save(withdrawal);

        User worker = withdrawal.getUser();
        addMileage(worker, MileageType.WITHDRAWAL_CANCELLED,
                withdrawal.getRequestAmount(), "출금 거절 환불", withdrawalId);
    }

    @Transactional(readOnly = true)
    public List<MileageWithdrawalResponseDto> getMyWithdrawals(User loginUser) {
        if (loginUser.getRole() != Role.INDIVIDUAL) {
            throw new CustomException(ErrorCode.WORKER_ONLY);
        }
        return mileageWithdrawalRepository.findByUserOrderByCreatedAtDesc(loginUser)
                .stream().map(MileageWithdrawalResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MileageWithdrawalResponseDto> getPendingWithdrawals(User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        return mileageWithdrawalRepository
                .findByStatus(MileageWithdrawalStatus.PENDING)
                .stream().map(MileageWithdrawalResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MileageWithdrawalResponseDto> getAllWithdrawals(
            MileageWithdrawalStatus status, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        List<MileageWithdrawal> list = (status != null)
                ? mileageWithdrawalRepository.findByStatus(status)
                : mileageWithdrawalRepository.findAllByOrderByCreatedAtDesc();
        return list.stream()
                .map(MileageWithdrawalResponseDto::new)
                .collect(Collectors.toList());
    }

    // 마일리지 수동 지급/차감 (ADMIN)
    @Transactional
    public void adjustMileage(Long targetUserId, int amount,
            String description, User loginUser) {
        if (loginUser.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ONLY);
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        addMileage(
                target,
                amount > 0 ? MileageType.REFERRAL_BONUS : MileageType.NO_SHOW,
                amount,
                "[관리자 지급] " + description,
                null
        );
    }
}
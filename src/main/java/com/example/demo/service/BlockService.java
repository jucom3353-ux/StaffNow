package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Block;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.BlockRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void blockUser(Long blockedId, User loginUser) {
        if (loginUser.getId().equals(blockedId)) {
            throw new CustomException(ErrorCode.SELF_BLOCK_NOT_ALLOWED);
        }

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (blockRepository.existsByBlockerAndBlocked(loginUser, blocked)) {
            throw new CustomException(ErrorCode.ALREADY_BLOCKED);
        }

        Block block = new Block();
        block.setBlocker(loginUser);
        block.setBlocked(blocked);
        blockRepository.save(block);
    }

    @Transactional
    public void unblockUser(Long blockedId, User loginUser) {
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!blockRepository.existsByBlockerAndBlocked(loginUser, blocked)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "차단하지 않은 사용자입니다.");
        }

        blockRepository.deleteByBlockerAndBlocked(loginUser, blocked);
    }

    @Transactional(readOnly = true)
    public List<Long> getBlockedUserIds(User loginUser) {
        return blockRepository.findByBlocker(loginUser)
                .stream()
                .map(b -> b.getBlocked().getId())
                .collect(Collectors.toList());
    }
}
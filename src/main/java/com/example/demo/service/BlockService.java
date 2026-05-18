package com.example.demo.service;

import com.example.demo.entity.Block;
import com.example.demo.entity.User;
import com.example.demo.repository.BlockRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;

    // 차단
    @Transactional
    public void blockUser(Long blockedId, User loginUser) {

        if (loginUser.getId().equals(blockedId)) {
            throw new RuntimeException("본인을 차단할 수 없습니다.");
        }

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (blockRepository.existsByBlockerAndBlocked(loginUser, blocked)) {
            throw new RuntimeException("이미 차단한 사용자입니다.");
        }

        Block block = new Block();
        block.setBlocker(loginUser);
        block.setBlocked(blocked);
        blockRepository.save(block);
    }

    // 차단 해제
    @Transactional
    public void unblockUser(Long blockedId, User loginUser) {

        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (!blockRepository.existsByBlockerAndBlocked(loginUser, blocked)) {
            throw new RuntimeException("차단하지 않은 사용자입니다.");
        }

        blockRepository.deleteByBlockerAndBlocked(loginUser, blocked);
    }

    // 내가 차단한 목록 조회
    @Transactional(readOnly = true)
    public List<Long> getBlockedUserIds(User loginUser) {
        return blockRepository.findByBlocker(loginUser)
                .stream()
                .map(b -> b.getBlocked().getId())
                .collect(Collectors.toList());
    }
}
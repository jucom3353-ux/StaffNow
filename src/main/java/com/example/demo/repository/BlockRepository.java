package com.example.demo.repository;

import com.example.demo.entity.Block;
import com.example.demo.entity.User;
import com.example.demo.util.AuthorizationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    // 내가 차단한 목록
    List<Block> findByBlocker(User blocker);

    // 차단 여부 확인
    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    // 차단 해제용
    void deleteByBlockerAndBlocked(User blocker, User blocked);
}
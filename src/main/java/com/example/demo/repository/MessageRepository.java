package com.example.demo.repository;

import com.example.demo.entity.Message;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // 두 유저 간 대화 조회 (시간순)
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :userA AND m.receiver = :userB) OR " +
           "(m.sender = :userB AND m.receiver = :userA) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversation(
            @Param("userA") User userA,
            @Param("userB") User userB
    );

    // 내 대화 상대 목록 (최신 메시지 기준)
    @Query("SELECT DISTINCT CASE WHEN m.sender = :user THEN m.receiver ELSE m.sender END " +
           "FROM Message m WHERE m.sender = :user OR m.receiver = :user")
    List<User> findConversationPartners(@Param("user") User user);

    // 안 읽은 메시지 수
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :user AND m.readAt IS NULL")
    int countUnread(@Param("user") User user);

    // 특정 대화 안 읽은 메시지 일괄 읽음 처리용
    @Query("SELECT m FROM Message m WHERE m.receiver = :receiver AND m.sender = :sender AND m.readAt IS NULL")
    List<Message> findUnreadMessages(
            @Param("receiver") User receiver,
            @Param("sender") User sender
    );
}
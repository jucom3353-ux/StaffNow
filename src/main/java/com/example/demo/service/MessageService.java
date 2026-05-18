package com.example.demo.service;

import com.example.demo.dto.ConversationResponseDto;
import com.example.demo.dto.MessageRequestDto;
import com.example.demo.dto.MessageResponseDto;
import com.example.demo.entity.Message;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.User;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // 메시지 전송
    @Transactional
    public MessageResponseDto sendMessage(MessageRequestDto requestDto, User loginUser) {

        if (requestDto.getContent() == null || requestDto.getContent().isBlank()) {
            throw new RuntimeException("메시지 내용을 입력해주세요.");
        }

        User receiver = userRepository.findById(requestDto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("수신자 없음"));

        if (receiver.getId().equals(loginUser.getId())) {
            throw new RuntimeException("본인에게 메시지를 보낼 수 없습니다.");
        }

        Message message = new Message();
        message.setSender(loginUser);
        message.setReceiver(receiver);
        message.setContent(requestDto.getContent());

        MessageResponseDto saved = new MessageResponseDto(messageRepository.save(message));

        // 알림 전송
        notificationService.send(
                receiver,
                NotificationType.MESSAGE_RECEIVED,
                loginUser.getName() + "님이 메시지를 보냈습니다.",
                message.getId()
        );

        return saved;
    }

    // 대화 조회 + 읽음 처리
    @Transactional
    public List<MessageResponseDto> getConversation(Long partnerId, User loginUser) {

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("상대방 없음"));

        List<Message> unread = messageRepository.findUnreadMessages(loginUser, partner);
        unread.forEach(m -> m.setReadAt(LocalDateTime.now()));
        messageRepository.saveAll(unread);

        return messageRepository.findConversation(loginUser, partner)
                .stream()
                .map(MessageResponseDto::new)
                .collect(Collectors.toList());
    }

    // 대화 상대 목록 (마지막 메시지 + 안읽은 수 포함)
    @Transactional(readOnly = true)
    public List<ConversationResponseDto> getConversationPartners(User loginUser) {
        return messageRepository.findConversationPartners(loginUser)
                .stream()
                .map(partner -> {
                    List<Message> lastMessages = messageRepository
                            .findLastMessage(loginUser, partner);

                    String lastMessage = lastMessages.isEmpty()
                            ? null : lastMessages.get(0).getContent();
                    LocalDateTime lastMessageTime = lastMessages.isEmpty()
                            ? null : lastMessages.get(0).getCreatedAt();

                    int unreadCount = (int) messageRepository
                            .findUnreadMessages(loginUser, partner).size();

                    return new ConversationResponseDto(
                            partner, lastMessage, lastMessageTime, unreadCount);
                })
                .collect(Collectors.toList());
    }

    // 안 읽은 메시지 수
    @Transactional(readOnly = true)
    public int getUnreadCount(User loginUser) {
        return messageRepository.countUnread(loginUser);
    }

    // 메시지 삭제 (본인이 보낸 것만)
    @Transactional
    public void deleteMessage(Long messageId, User loginUser) {
        Message message = messageRepository.findByIdAndSender(messageId, loginUser)
                .orElseThrow(() -> new RuntimeException("메시지 없음 또는 권한 없음"));

        messageRepository.delete(message);
    }
}
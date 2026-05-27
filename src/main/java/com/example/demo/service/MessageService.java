package com.example.demo.service;

import com.example.demo.dto.ConversationResponseDto;
import com.example.demo.dto.MessageRequestDto;
import com.example.demo.dto.MessageResponseDto;
import com.example.demo.entity.Message;
import com.example.demo.entity.NotificationType;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
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

    @Transactional
    public MessageResponseDto sendMessage(MessageRequestDto requestDto, User loginUser) {
        if (requestDto.getContent() == null || requestDto.getContent().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_MESSAGE_CONTENT);
        }

        User receiver = userRepository.findById(requestDto.getReceiverId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (receiver.getId().equals(loginUser.getId())) {
            throw new CustomException(ErrorCode.SELF_MESSAGE_NOT_ALLOWED);
        }

        // 구직자 → 기업 메시지 차단
        if (loginUser.getRole() == Role.INDIVIDUAL &&
                (receiver.getRole() == Role.COMPANY || receiver.getRole() == Role.MANAGER)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "기업에게 먼저 메시지를 보낼 수 없습니다.");
        }

        Message message = new Message();
        message.setSender(loginUser);
        message.setReceiver(receiver);
        message.setContent(requestDto.getContent());

        MessageResponseDto saved = new MessageResponseDto(messageRepository.save(message));

        notificationService.send(receiver, NotificationType.MESSAGE_RECEIVED,
                loginUser.getName() + "님이 메시지를 보냈습니다.", message.getId());

        return saved;
    }

    @Transactional
    public List<MessageResponseDto> getConversation(Long partnerId, User loginUser) {
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Message> unread = messageRepository.findUnreadMessages(loginUser, partner);
        unread.forEach(m -> m.setReadAt(LocalDateTime.now()));
        messageRepository.saveAll(unread);

        return messageRepository.findConversation(loginUser, partner).stream()
                .map(MessageResponseDto::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConversationResponseDto> getConversationPartners(User loginUser) {
        return messageRepository.findConversationPartners(loginUser).stream()
                .map(partner -> {
                    List<Message> lastMessages = messageRepository.findLastMessage(loginUser, partner);
                    String lastMessage = lastMessages.isEmpty() ? null : lastMessages.get(0).getContent();
                    LocalDateTime lastMessageTime = lastMessages.isEmpty()
                            ? null : lastMessages.get(0).getCreatedAt();
                    int unreadCount = (int) messageRepository.findUnreadMessages(loginUser, partner).size();
                    return new ConversationResponseDto(partner, lastMessage, lastMessageTime, unreadCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(User loginUser) {
        return messageRepository.countUnread(loginUser);
    }

    @Transactional
    public void deleteMessage(Long messageId, User loginUser) {
        Message message = messageRepository.findByIdAndSender(messageId, loginUser)
                .orElseThrow(() -> new CustomException(ErrorCode.MESSAGE_NOT_FOUND));

        messageRepository.delete(message);
    }
}
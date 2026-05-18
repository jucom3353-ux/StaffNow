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

    @Transactional(readOnly = true)
    public List<ConversationResponseDto> getConversationPartners(User loginUser) {
        return messageRepository.findConversationPartners(loginUser)
                .stream()
                .map(ConversationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(User loginUser) {
        return messageRepository.countUnread(loginUser);
    }
}
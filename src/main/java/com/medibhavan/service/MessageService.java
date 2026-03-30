package com.medibhavan.service;

import com.medibhavan.dto.request.SendMessageRequest;
import com.medibhavan.dto.response.ChatMessageResponse;
import com.medibhavan.dto.response.UserResponse;
import com.medibhavan.exception.BadRequestException;
import com.medibhavan.exception.ResourceNotFoundException;
import com.medibhavan.model.Connection;
import com.medibhavan.model.Message;
import com.medibhavan.repository.MessageRepository;
import com.medibhavan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ConnectionService connectionService;

    // ── Get all messages for a connection ────────────
    public List<ChatMessageResponse> getMessages(String connectionId, String currentUserId) {
        Connection connection = connectionService.getConnectionById(connectionId);
        assertMember(connection, currentUserId);

        // Mark incoming messages as read
        List<Message> unread = messageRepository
                .findByConnectionIdAndReceiverIdAndReadFalse(connectionId, currentUserId);
        unread.forEach(m -> m.setRead(true));
        if (!unread.isEmpty()) messageRepository.saveAll(unread);

        return messageRepository
                .findByConnectionIdOrderByCreatedAtAsc(connectionId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Send a message ───────────────────────────────
    public ChatMessageResponse sendMessage(SendMessageRequest request, String senderId) {
        Connection connection = connectionService.getConnectionById(request.getConnectionId());

        if (!"active".equals(connection.getStatus())) {
            throw new BadRequestException("Cannot send messages in an inactive connection.");
        }

        assertMember(connection, senderId);

        // The other person in the connection is the receiver
        String receiverId = connection.getDoctorId().equals(senderId)
                ? connection.getPatientId()
                : connection.getDoctorId();

        Message message = messageRepository.save(
                Message.builder()
                        .connectionId(request.getConnectionId())
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .content(request.getContent().trim())
                        .build()
        );

        return toResponse(message);
    }

    // ── Unread message count ─────────────────────────
    public long getUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndReadFalse(userId);
    }

    // ── Helpers ──────────────────────────────────────
    private void assertMember(Connection connection, String userId) {
        boolean member = connection.getDoctorId().equals(userId)
                || connection.getPatientId().equals(userId);
        if (!member) {
            throw new BadRequestException("You are not part of this connection.");
        }
    }

    private ChatMessageResponse toResponse(Message m) {
        ChatMessageResponse r = new ChatMessageResponse();
        r.setId(m.getId());
        r.setConnectionId(m.getConnectionId());
        r.setReceiverId(m.getReceiverId());
        r.setContent(m.getContent());
        r.setRead(m.isRead());
        r.setCreatedAt(m.getCreatedAt());

        userRepository.findById(m.getSenderId())
                .ifPresent(u -> r.setSender(UserResponse.from(u)));

        return r;
    }
}

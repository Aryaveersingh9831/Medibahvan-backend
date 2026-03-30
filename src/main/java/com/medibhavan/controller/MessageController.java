package com.medibhavan.controller;

import com.medibhavan.dto.request.SendMessageRequest;
import com.medibhavan.dto.response.ChatMessageResponse;
import com.medibhavan.dto.response.UnreadCountResponse;
import com.medibhavan.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // GET /api/messages/unread  — how many unread messages
    @GetMapping("/unread")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = messageService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(new UnreadCountResponse(count));
    }

    // GET /api/messages/:connectionId  — fetch all messages, marks them read
    @GetMapping("/{connectionId}")
    public ResponseEntity<Map<String, Object>> getMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String connectionId) {

        List<ChatMessageResponse> messages =
                messageService.getMessages(connectionId, userDetails.getUsername());

        return ResponseEntity.ok(Map.of("messages", messages));
    }

    // POST /api/messages  — send a message
    @PostMapping
    public ResponseEntity<Map<String, Object>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {

        ChatMessageResponse message =
                messageService.sendMessage(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", message));
    }
}

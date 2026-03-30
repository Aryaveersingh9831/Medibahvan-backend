package com.medibhavan.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {
    private String id;
    private String connectionId;
    private UserResponse sender;
    private String receiverId;
    private String content;
    private boolean read;
    private LocalDateTime createdAt;
}

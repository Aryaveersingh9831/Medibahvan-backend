package com.medibhavan.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    private String connectionId;
    private String senderId;
    private String receiverId;
    private String content;

    @Builder.Default
    private boolean read = false;

    @CreatedDate
    private LocalDateTime createdAt;
}

package com.medibhavan.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Connection ID is required")
    private String connectionId;

    @NotBlank(message = "Message content is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String content;
}

package com.medibhavan.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectRequest {

    @NotBlank(message = "Target user ID is required")
    private String targetUserId;
}

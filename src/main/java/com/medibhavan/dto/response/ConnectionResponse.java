package com.medibhavan.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConnectionResponse {
    private String id;
    private UserResponse doctor;
    private UserResponse patient;
    private String status;
    private LocalDateTime connectedAt;
}

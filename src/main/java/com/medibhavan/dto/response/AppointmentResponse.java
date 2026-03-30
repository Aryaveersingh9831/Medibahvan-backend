package com.medibhavan.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponse {
    private String id;
    private String connectionId;
    private UserResponse doctor;
    private UserResponse patient;
    private String date;
    private String time;
    private String type;
    private String status;
    private String notes;
    private String requestedBy;
    private LocalDateTime createdAt;
}

package com.medibhavan.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    private String id;

    private String connectionId;
    private String doctorId;
    private String patientId;

    private String date;  // YYYY-MM-DD
    private String time;  // HH:MM

    // "consultation" | "follow-up" | "checkup" | "emergency" | "other"
    @Builder.Default
    private String type = "consultation";

    // "pending" | "confirmed" | "cancelled" | "completed"
    @Builder.Default
    private String status = "pending";

    @Builder.Default
    private String notes = "";

    // "doctor" or "patient"
    private String requestedBy;

    @CreatedDate
    private LocalDateTime createdAt;
}

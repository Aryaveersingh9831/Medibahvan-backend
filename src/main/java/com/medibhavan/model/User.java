package com.medibhavan.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String name;

    // "doctor" or "patient"
    private String role;

    // Auto-generated: Dr_XXXXXX or P_XXXXXX
    @Indexed(unique = true)
    private String userId;

    // ── Doctor-specific ──────────────────────
    private String specialization;
    private String licenseNumber;
    private String hospital;

    // ── Patient-specific ─────────────────────
    private String dateOfBirth;
    private String bloodGroup;
    private String allergies;

    // ── Common ───────────────────────────────
    private String phone;
    private String address;
    private String gender;

    @CreatedDate
    private LocalDateTime createdAt;
}

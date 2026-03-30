package com.medibhavan.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalFile {

    @Id
    private String id;

    private String originalName;
    private String storedName;
    private String path;
    private Long size;         // bytes
    private String mimetype;

    // "prescription" | "report" | "test" | "xray" | "scan" | "other"
    @Builder.Default
    private String type = "other";

    private String uploadedBy;  // User ID
    private String patientId;   // User ID
    private String doctorId;    // User ID (nullable)

    @Builder.Default
    private String notes = "";

    @CreatedDate
    private LocalDateTime createdAt;
}

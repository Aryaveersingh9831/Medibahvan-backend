package com.medibhavan.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileResponse {
    private String id;
    private String originalName;
    private String type;
    private Long size;
    private String notes;
    private UserResponse uploadedBy;
    private String patientId;
    private String doctorId;
    private LocalDateTime createdAt;

    public String getFormattedSize() {
        if (size == null) return "";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }
}

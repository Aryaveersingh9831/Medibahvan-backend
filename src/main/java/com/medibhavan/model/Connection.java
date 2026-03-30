package com.medibhavan.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(def = "{'doctorId': 1, 'patientId': 1}", unique = true)
public class Connection {

    @Id
    private String id;

    // Stored as String IDs — populated manually in service layer
    private String doctorId;
    private String patientId;

    // "active" or "inactive"
    @Builder.Default
    private String status = "active";

    @CreatedDate
    private LocalDateTime connectedAt;
}

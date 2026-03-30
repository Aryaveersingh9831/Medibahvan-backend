package com.medibhavan.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAppointmentRequest {

    @NotBlank(message = "Connection ID is required")
    private String connectionId;

    @NotBlank(message = "Date is required")
    private String date;

    @NotBlank(message = "Time is required")
    private String time;

    private String type;
    private String notes;
}

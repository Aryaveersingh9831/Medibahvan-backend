package com.medibhavan.dto.request;

import lombok.Data;

@Data
public class UpdateAppointmentRequest {
    private String status;
    private String notes;
    private String date;
    private String time;
    private String type;
}

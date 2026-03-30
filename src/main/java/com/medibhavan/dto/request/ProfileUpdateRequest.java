package com.medibhavan.dto.request;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String name;
    private String phone;
    private String address;
    private String gender;
    // Doctor-specific
    private String specialization;
    private String licenseNumber;
    private String hospital;
    // Patient-specific
    private String dateOfBirth;
    private String bloodGroup;
    private String allergies;
}

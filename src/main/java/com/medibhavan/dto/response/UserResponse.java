package com.medibhavan.dto.response;

import com.medibhavan.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private String id;
    private String email;
    private String name;
    private String role;
    private String userId;
    private String specialization;
    private String licenseNumber;
    private String hospital;
    private String dateOfBirth;
    private String bloodGroup;
    private String allergies;
    private String phone;
    private String address;
    private String gender;
    private LocalDateTime createdAt;

    // Convert User entity → safe response (no password)
    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id            = user.getId();
        r.email         = user.getEmail();
        r.name          = user.getName();
        r.role          = user.getRole();
        r.userId        = user.getUserId();
        r.specialization= user.getSpecialization();
        r.licenseNumber = user.getLicenseNumber();
        r.hospital      = user.getHospital();
        r.dateOfBirth   = user.getDateOfBirth();
        r.bloodGroup    = user.getBloodGroup();
        r.allergies     = user.getAllergies();
        r.phone         = user.getPhone();
        r.address       = user.getAddress();
        r.gender        = user.getGender();
        r.createdAt     = user.getCreatedAt();
        return r;
    }
}

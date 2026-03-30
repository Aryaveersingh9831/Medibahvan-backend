package com.medibhavan.service;

import com.medibhavan.dto.request.*;
import com.medibhavan.dto.response.*;
import com.medibhavan.exception.BadRequestException;
import com.medibhavan.exception.ResourceNotFoundException;
import com.medibhavan.model.User;
import com.medibhavan.repository.UserRepository;
import com.medibhavan.security.JwtUtil;
import com.medibhavan.util.UserIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserIdGenerator userIdGenerator;

    // ── Register ─────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        // Check email not already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists.");
        }

        // Generate unique userId (retry if collision — extremely rare)
        String userId;
        int attempts = 0;
        do {
            userId = userIdGenerator.generate(request.getRole());
            attempts++;
            if (attempts > 10) throw new BadRequestException("Could not generate unique user ID. Try again.");
        } while (userRepository.findByUserId(userId).isPresent());

        // Build and save user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName().trim())
                .role(request.getRole())
                .userId(userId)
                .build();

        user = userRepository.save(user);
        log.info("New {} registered: {} ({})", user.getRole(), user.getName(), user.getUserId());

        String token = jwtUtil.generateToken(user.getId());
        return new AuthResponse(token, UserResponse.from(user));
    }

    // ── Login ────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getId());
        log.info("User logged in: {} ({})", user.getName(), user.getRole());
        return new AuthResponse(token, UserResponse.from(user));
    }

    // ── Get current user ─────────────────────────────
    public UserResponse getMe(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return UserResponse.from(user);
    }

    // ── Update profile ───────────────────────────────
    public UserResponse updateProfile(String userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Only update fields that were actually sent (non-null)
        if (request.getName()           != null) user.setName(request.getName());
        if (request.getPhone()          != null) user.setPhone(request.getPhone());
        if (request.getAddress()        != null) user.setAddress(request.getAddress());
        if (request.getGender()         != null) user.setGender(request.getGender());
        if (request.getSpecialization() != null) user.setSpecialization(request.getSpecialization());
        if (request.getLicenseNumber()  != null) user.setLicenseNumber(request.getLicenseNumber());
        if (request.getHospital()       != null) user.setHospital(request.getHospital());
        if (request.getDateOfBirth()    != null) user.setDateOfBirth(request.getDateOfBirth());
        if (request.getBloodGroup()     != null) user.setBloodGroup(request.getBloodGroup());
        if (request.getAllergies()       != null) user.setAllergies(request.getAllergies());

        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    // ── Change password ──────────────────────────────
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }
}

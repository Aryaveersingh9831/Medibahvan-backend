package com.medibhavan.service;

import com.medibhavan.dto.request.ConnectRequest;
import com.medibhavan.dto.response.ConnectionResponse;
import com.medibhavan.dto.response.UserResponse;
import com.medibhavan.exception.BadRequestException;
import com.medibhavan.exception.ResourceNotFoundException;
import com.medibhavan.model.Connection;
import com.medibhavan.model.User;
import com.medibhavan.repository.ConnectionRepository;
import com.medibhavan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;

    // ── Find user by their userId string (Dr_XXX / P_XXX) ──
    public UserResponse findByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with ID: " + userId));
        return UserResponse.from(user);
    }

    // ── Create or reactivate a connection ───────────
    public ConnectionResponse createConnection(String currentUserId, ConnectRequest request) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));

        User targetUser = userRepository.findByUserId(request.getTargetUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user found with ID: " + request.getTargetUserId()));

        // Determine doctor and patient
        String doctorId, patientId;

        if ("doctor".equals(currentUser.getRole())) {
            if (!"patient".equals(targetUser.getRole())) {
                throw new BadRequestException("Doctors can only connect with patients.");
            }
            doctorId  = currentUser.getId();
            patientId = targetUser.getId();
        } else {
            if (!"doctor".equals(targetUser.getRole())) {
                throw new BadRequestException("Patients can only connect with doctors.");
            }
            doctorId  = targetUser.getId();
            patientId = currentUser.getId();
        }

        // Upsert: find existing or create new
        Optional<Connection> existing = connectionRepository
                .findByDoctorIdAndPatientId(doctorId, patientId);

        Connection connection;
        if (existing.isPresent()) {
            connection = existing.get();
            connection.setStatus("active"); // reactivate if previously disconnected
            connection = connectionRepository.save(connection);
        } else {
            connection = connectionRepository.save(
                    Connection.builder()
                            .doctorId(doctorId)
                            .patientId(patientId)
                            .status("active")
                            .build()
            );
        }

        log.info("Connection {} <-> {}", currentUser.getUserId(), targetUser.getUserId());
        return toResponse(connection);
    }

    // ── List all active connections for current user ─
    public List<ConnectionResponse> listConnections(String currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        List<Connection> connections;
        if ("doctor".equals(user.getRole())) {
            connections = connectionRepository
                    .findByDoctorIdAndStatus(currentUserId, "active");
        } else {
            connections = connectionRepository
                    .findByPatientIdAndStatus(currentUserId, "active");
        }

        return connections.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Disconnect ───────────────────────────────────
    public void disconnect(String connectionId, String currentUserId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found."));

        boolean authorized = connection.getDoctorId().equals(currentUserId)
                || connection.getPatientId().equals(currentUserId);

        if (!authorized) {
            throw new BadRequestException("You are not part of this connection.");
        }

        connection.setStatus("inactive");
        connectionRepository.save(connection);
    }

    // ── Helper: entity → response ────────────────────
    public ConnectionResponse toResponse(Connection conn) {
        ConnectionResponse r = new ConnectionResponse();
        r.setId(conn.getId());
        r.setStatus(conn.getStatus());
        r.setConnectedAt(conn.getConnectedAt());

        userRepository.findById(conn.getDoctorId())
                .ifPresent(d -> r.setDoctor(UserResponse.from(d)));
        userRepository.findById(conn.getPatientId())
                .ifPresent(p -> r.setPatient(UserResponse.from(p)));

        return r;
    }

    // ── Lookup connection by ID (used by other services) ──
    public Connection getConnectionById(String connectionId) {
        return connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found."));
    }
}

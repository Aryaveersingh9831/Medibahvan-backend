package com.medibhavan.service;

import com.medibhavan.dto.request.CreateAppointmentRequest;
import com.medibhavan.dto.request.UpdateAppointmentRequest;
import com.medibhavan.dto.response.AppointmentResponse;
import com.medibhavan.dto.response.UserResponse;
import com.medibhavan.exception.BadRequestException;
import com.medibhavan.exception.ResourceNotFoundException;
import com.medibhavan.model.Appointment;
import com.medibhavan.model.Connection;
import com.medibhavan.model.User;
import com.medibhavan.repository.AppointmentRepository;
import com.medibhavan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ConnectionService connectionService;

    // ── List appointments for current user ───────────
    public List<AppointmentResponse> getAppointments(String userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        List<Appointment> appointments;

        if ("doctor".equals(user.getRole())) {
            appointments = (status != null && !status.equals("all"))
                    ? appointmentRepository.findByDoctorIdAndStatusOrderByDateAscTimeAsc(userId, status)
                    : appointmentRepository.findByDoctorIdOrderByDateAscTimeAsc(userId);
        } else {
            appointments = (status != null && !status.equals("all"))
                    ? appointmentRepository.findByPatientIdAndStatusOrderByDateAscTimeAsc(userId, status)
                    : appointmentRepository.findByPatientIdOrderByDateAscTimeAsc(userId);
        }

        return appointments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Create appointment ───────────────────────────
    public AppointmentResponse createAppointment(CreateAppointmentRequest request,
                                                  String currentUserId) {
        Connection connection = connectionService.getConnectionById(request.getConnectionId());

        if (!"active".equals(connection.getStatus())) {
            throw new BadRequestException("Cannot schedule appointment for an inactive connection.");
        }

        // Verify current user is part of this connection
        boolean isMember = connection.getDoctorId().equals(currentUserId)
                || connection.getPatientId().equals(currentUserId);
        if (!isMember) {
            throw new BadRequestException("You are not part of this connection.");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Appointment appointment = appointmentRepository.save(
                Appointment.builder()
                        .connectionId(request.getConnectionId())
                        .doctorId(connection.getDoctorId())
                        .patientId(connection.getPatientId())
                        .date(request.getDate())
                        .time(request.getTime())
                        .type(request.getType() != null ? request.getType() : "consultation")
                        .notes(request.getNotes() != null ? request.getNotes() : "")
                        .requestedBy(currentUser.getRole())
                        .build()
        );

        log.info("Appointment created: {} on {} at {}", appointment.getId(),
                appointment.getDate(), appointment.getTime());

        return toResponse(appointment);
    }

    // ── Update appointment ───────────────────────────
    public AppointmentResponse updateAppointment(String appointmentId,
                                                  UpdateAppointmentRequest request,
                                                  String currentUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));

        boolean isMember = appointment.getDoctorId().equals(currentUserId)
                || appointment.getPatientId().equals(currentUserId);
        if (!isMember) {
            throw new BadRequestException("You are not authorized to update this appointment.");
        }

        if (request.getStatus() != null) appointment.setStatus(request.getStatus());
        if (request.getNotes()  != null) appointment.setNotes(request.getNotes());
        if (request.getDate()   != null) appointment.setDate(request.getDate());
        if (request.getTime()   != null) appointment.setTime(request.getTime());
        if (request.getType()   != null) appointment.setType(request.getType());

        appointment = appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    // ── Cancel appointment ───────────────────────────
    public void cancelAppointment(String appointmentId, String currentUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));

        boolean isMember = appointment.getDoctorId().equals(currentUserId)
                || appointment.getPatientId().equals(currentUserId);
        if (!isMember) {
            throw new BadRequestException("You are not authorized to cancel this appointment.");
        }

        appointment.setStatus("cancelled");
        appointmentRepository.save(appointment);
        log.info("Appointment {} cancelled by {}", appointmentId, currentUserId);
    }

    // ── Helper: entity → response ────────────────────
    private AppointmentResponse toResponse(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.setId(a.getId());
        r.setConnectionId(a.getConnectionId());
        r.setDate(a.getDate());
        r.setTime(a.getTime());
        r.setType(a.getType());
        r.setStatus(a.getStatus());
        r.setNotes(a.getNotes());
        r.setRequestedBy(a.getRequestedBy());
        r.setCreatedAt(a.getCreatedAt());

        userRepository.findById(a.getDoctorId())
                .ifPresent(d -> r.setDoctor(UserResponse.from(d)));
        userRepository.findById(a.getPatientId())
                .ifPresent(p -> r.setPatient(UserResponse.from(p)));

        return r;
    }
}

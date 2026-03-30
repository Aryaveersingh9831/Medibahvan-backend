package com.medibhavan.controller;

import com.medibhavan.dto.request.CreateAppointmentRequest;
import com.medibhavan.dto.request.UpdateAppointmentRequest;
import com.medibhavan.dto.response.AppointmentResponse;
import com.medibhavan.dto.response.MessageResponse;
import com.medibhavan.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // GET /api/appointments?status=pending
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAppointments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "status", required = false) String status) {

        List<AppointmentResponse> appointments =
                appointmentService.getAppointments(userDetails.getUsername(), status);

        return ResponseEntity.ok(Map.of("appointments", appointments));
    }

    // POST /api/appointments
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateAppointmentRequest request) {

        AppointmentResponse appointment =
                appointmentService.createAppointment(request, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("appointment", appointment));
    }

    // PUT /api/appointments/:id
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody UpdateAppointmentRequest request) {

        AppointmentResponse appointment =
                appointmentService.updateAppointment(id, request, userDetails.getUsername());

        return ResponseEntity.ok(Map.of("appointment", appointment));
    }

    // DELETE /api/appointments/:id  — cancel
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        appointmentService.cancelAppointment(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Appointment cancelled."));
    }
}

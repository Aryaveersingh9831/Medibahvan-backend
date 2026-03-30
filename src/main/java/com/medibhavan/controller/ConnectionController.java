package com.medibhavan.controller;

import com.medibhavan.dto.request.ConnectRequest;
import com.medibhavan.dto.response.*;
import com.medibhavan.service.ConnectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    // GET /api/connections/find/:userId  — look up by userId string (Dr_XXX / P_XXX)
    @GetMapping("/find/{userId}")
    public ResponseEntity<Map<String, Object>> findByUserId(
            @PathVariable String userId) {
        UserResponse user = connectionService.findByUserId(userId);
        return ResponseEntity.ok(Map.of("user", user));
    }

    // POST /api/connections  — connect with another user
    @PostMapping
    public ResponseEntity<ConnectionResponse> createConnection(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ConnectRequest request) {
        ConnectionResponse conn = connectionService
                .createConnection(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(conn);
    }

    // GET /api/connections  — list all my connections
    @GetMapping
    public ResponseEntity<Map<String, Object>> listConnections(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<ConnectionResponse> connections =
                connectionService.listConnections(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("connections", connections));
    }

    // DELETE /api/connections/:id  — disconnect
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> disconnect(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        connectionService.disconnect(id, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("Disconnected successfully."));
    }
}

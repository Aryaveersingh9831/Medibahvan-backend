package com.medibhavan.controller;

import com.medibhavan.dto.response.FileResponse;
import com.medibhavan.dto.response.MessageResponse;
import com.medibhavan.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // POST /api/files/upload  — multipart form upload
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file")              MultipartFile file,
            @RequestParam(value = "type",      required = false) String type,
            @RequestParam(value = "notes",     required = false) String notes,
            @RequestParam(value = "patientId", required = false) String patientId,
            @RequestParam(value = "doctorId",  required = false) String doctorId) {

        FileResponse uploaded = fileService.uploadFile(
                file, type, notes, patientId, doctorId, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("file", uploaded));
    }

    // GET /api/files/my  — patient gets their own files
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyFiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "type", required = false) String type) {

        List<FileResponse> files = fileService.getMyFiles(userDetails.getUsername(), type);
        return ResponseEntity.ok(Map.of("files", files));
    }

    // GET /api/files/patient/:patientId  — doctor views a patient's files
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Map<String, Object>> getPatientFiles(
            @PathVariable String patientId,
            @RequestParam(value = "type", required = false) String type) {

        List<FileResponse> files = fileService.getPatientFiles(patientId, type);
        return ResponseEntity.ok(Map.of("files", files));
    }

    // GET /api/files/download/:fileId  — download with auth
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String fileId) {

        Resource resource = fileService.downloadFile(fileId, userDetails.getUsername());
        String filename   = fileService.getOriginalFilename(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    // DELETE /api/files/:fileId
    @DeleteMapping("/{fileId}")
    public ResponseEntity<MessageResponse> deleteFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String fileId) {

        fileService.deleteFile(fileId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponse("File deleted successfully."));
    }
}

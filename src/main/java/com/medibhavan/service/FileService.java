package com.medibhavan.service;

import com.medibhavan.dto.response.FileResponse;
import com.medibhavan.dto.response.UserResponse;
import com.medibhavan.exception.BadRequestException;
import com.medibhavan.exception.ResourceNotFoundException;
import com.medibhavan.model.MedicalFile;
import com.medibhavan.model.User;
import com.medibhavan.repository.MedicalFileRepository;
import com.medibhavan.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final MedicalFileRepository fileRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of(".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".txt");

    // ── Create uploads directory on startup ──────────
    @PostConstruct
    public void init() {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            log.info("Upload directory ready: {}", uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    // ── Upload a file ────────────────────────────────
    public FileResponse uploadFile(MultipartFile file,
                                   String type,
                                   String notes,
                                   String patientId,
                                   String doctorId,
                                   String uploadedByUserId) {

        User uploader = userRepository.findById(uploadedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Uploader not found."));

        // Resolve patientId
        String resolvedPatientId = "patient".equals(uploader.getRole())
                ? uploadedByUserId
                : patientId;

        if (resolvedPatientId == null || resolvedPatientId.isBlank()) {
            throw new BadRequestException("Patient ID is required for doctor uploads.");
        }

        // Validate file
        String originalFilename = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename(), "Filename is missing"));

        String ext = getExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BadRequestException(
                    "File type not allowed. Supported: PDF, JPG, PNG, DOC, DOCX");
        }

        // Generate unique stored filename
        String storedName = UUID.randomUUID() + ext;
        Path targetPath = uploadPath.resolve(storedName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BadRequestException("Failed to save file: " + e.getMessage());
        }

        // Save record to DB
        MedicalFile medicalFile = MedicalFile.builder()
                .originalName(originalFilename)
                .storedName(storedName)
                .path(targetPath.toString())
                .size(file.getSize())
                .mimetype(file.getContentType())
                .type(type != null ? type : "other")
                .uploadedBy(uploadedByUserId)
                .patientId(resolvedPatientId)
                .doctorId("doctor".equals(uploader.getRole()) ? uploadedByUserId : doctorId)
                .notes(notes != null ? notes : "")
                .build();

        medicalFile = fileRepository.save(medicalFile);
        log.info("File uploaded: {} by {}", originalFilename, uploader.getUserId());

        return toResponse(medicalFile);
    }

    // ── Get patient's files (called by patient) ──────
    public List<FileResponse> getMyFiles(String patientId, String type) {
        List<MedicalFile> files = (type != null && !type.equals("all"))
                ? fileRepository.findByPatientIdAndTypeOrderByCreatedAtDesc(patientId, type)
                : fileRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Get patient's files (called by doctor) ───────
    public List<FileResponse> getPatientFiles(String patientId, String type) {
        List<MedicalFile> files = (type != null && !type.equals("all"))
                ? fileRepository.findByPatientIdAndTypeOrderByCreatedAtDesc(patientId, type)
                : fileRepository.findByPatientIdOrderByCreatedAtDesc(patientId);

        return files.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Download file as Resource ────────────────────
    public Resource downloadFile(String fileId, String requestingUserId) {
        MedicalFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found."));

        User requester = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Auth check: must be the patient or a doctor
        boolean isPatient = file.getPatientId().equals(requestingUserId);
        boolean isDoctor  = "doctor".equals(requester.getRole());

        if (!isPatient && !isDoctor) {
            throw new BadRequestException("Not authorized to download this file.");
        }

        try {
            Path filePath = Paths.get(file.getPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found on server.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found on server.");
        }
    }

    public String getOriginalFilename(String fileId) {
        return fileRepository.findById(fileId)
                .map(MedicalFile::getOriginalName)
                .orElse("download");
    }

    // ── Delete file ───────────────────────────────────
    public void deleteFile(String fileId, String requestingUserId) {
        MedicalFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found."));

        if (!file.getUploadedBy().equals(requestingUserId)) {
            throw new BadRequestException("Only the uploader can delete this file.");
        }

        // Delete from disk
        try {
            Files.deleteIfExists(Paths.get(file.getPath()));
        } catch (IOException e) {
            log.warn("Could not delete file from disk: {}", file.getPath());
        }

        fileRepository.delete(file);
        log.info("File deleted: {} by {}", file.getOriginalName(), requestingUserId);
    }

    // ── Helpers ──────────────────────────────────────
    private String getExtension(String filename) {
        int dotIdx = filename.lastIndexOf('.');
        return dotIdx >= 0 ? filename.substring(dotIdx) : "";
    }

    private FileResponse toResponse(MedicalFile file) {
        FileResponse r = new FileResponse();
        r.setId(file.getId());
        r.setOriginalName(file.getOriginalName());
        r.setType(file.getType());
        r.setSize(file.getSize());
        r.setNotes(file.getNotes());
        r.setPatientId(file.getPatientId());
        r.setDoctorId(file.getDoctorId());
        r.setCreatedAt(file.getCreatedAt());

        userRepository.findById(file.getUploadedBy())
                .ifPresent(u -> r.setUploadedBy(UserResponse.from(u)));

        return r;
    }
}

package com.medibhavan.repository;

import com.medibhavan.model.MedicalFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MedicalFileRepository extends MongoRepository<MedicalFile, String> {
    List<MedicalFile> findByPatientIdOrderByCreatedAtDesc(String patientId);
    List<MedicalFile> findByPatientIdAndTypeOrderByCreatedAtDesc(String patientId, String type);
}

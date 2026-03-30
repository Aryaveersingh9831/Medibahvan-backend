package com.medibhavan.repository;

import com.medibhavan.model.Connection;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends MongoRepository<Connection, String> {
    List<Connection> findByDoctorIdAndStatus(String doctorId, String status);
    List<Connection> findByPatientIdAndStatus(String patientId, String status);
    Optional<Connection> findByDoctorIdAndPatientId(String doctorId, String patientId);
}

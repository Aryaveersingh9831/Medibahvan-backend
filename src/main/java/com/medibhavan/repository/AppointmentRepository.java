package com.medibhavan.repository;

import com.medibhavan.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByDoctorIdOrderByDateAscTimeAsc(String doctorId);
    List<Appointment> findByPatientIdOrderByDateAscTimeAsc(String patientId);
    List<Appointment> findByDoctorIdAndStatusOrderByDateAscTimeAsc(String doctorId, String status);
    List<Appointment> findByPatientIdAndStatusOrderByDateAscTimeAsc(String patientId, String status);
}

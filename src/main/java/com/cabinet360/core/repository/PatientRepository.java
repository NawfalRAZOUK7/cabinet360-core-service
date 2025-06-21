// PatientRepository.java
package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByPatientUserId(Long patientUserId);
}

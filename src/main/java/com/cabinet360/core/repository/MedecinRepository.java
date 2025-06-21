package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Medecin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    Optional<Medecin> findByDoctorUserId(Long doctorUserId);
    boolean existsByDoctorUserId(Long doctorUserId);
}
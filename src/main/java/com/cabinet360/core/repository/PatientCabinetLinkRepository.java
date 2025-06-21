package com.cabinet360.core.repository;

import com.cabinet360.core.entity.PatientCabinetLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientCabinetLinkRepository extends JpaRepository<PatientCabinetLink, Long> {

    /**
     * Find all links for a specific patient
     */
    List<PatientCabinetLink> findByPatientUserId(Long patientUserId);

    /**
     * Find all links for a specific cabinet
     */
    List<PatientCabinetLink> findByCabinetId(Long cabinetId);

    /**
     * Find active links for a patient
     */
    List<PatientCabinetLink> findByPatientUserIdAndStatusAndDeletedAtIsNull(Long patientUserId, String status);

    /**
     * Find active links for a cabinet
     */
    List<PatientCabinetLink> findByCabinetIdAndStatusAndDeletedAtIsNull(Long cabinetId, String status);

    /**
     * Check if link exists
     */
    boolean existsByPatientUserIdAndCabinetId(Long patientUserId, Long cabinetId);

    /**
     * Find specific link
     */
    Optional<PatientCabinetLink> findByPatientUserIdAndCabinetId(Long patientUserId, Long cabinetId);

    /**
     * Find active link
     */
    Optional<PatientCabinetLink> findByPatientUserIdAndCabinetIdAndStatusAndDeletedAtIsNull(
            Long patientUserId, Long cabinetId, String status);

    /**
     * Delete link
     */
    void deleteByPatientUserIdAndCabinetId(Long patientUserId, Long cabinetId);

    /**
     * Count active links for patient
     */
    @Query("SELECT COUNT(l) FROM PatientCabinetLink l WHERE l.patientUserId = :patientUserId AND l.status = 'ACTIVE' AND l.deletedAt IS NULL")
    long countActiveLinksForPatient(@Param("patientUserId") Long patientUserId);

    /**
     * Count active links for cabinet
     */
    @Query("SELECT COUNT(l) FROM PatientCabinetLink l WHERE l.cabinetId = :cabinetId AND l.status = 'ACTIVE' AND l.deletedAt IS NULL")
    long countActiveLinksForCabinet(@Param("cabinetId") Long cabinetId);

    /**
     * Find links with recent access
     */
    List<PatientCabinetLink> findByLastAccessAtAfter(LocalDateTime date);

    /**
     * Find pending links
     */
    List<PatientCabinetLink> findByStatusAndDeletedAtIsNull(String status);
}

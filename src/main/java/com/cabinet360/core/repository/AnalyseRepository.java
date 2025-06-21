package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Analyse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyseRepository extends JpaRepository<Analyse, Long> {

    /**
     * Trouver toutes les analyses pour un dossier médical donné.
     */
    List<Analyse> findByDossierMedicalId(Long dossierMedicalId);

    /**
     * ✅ Fixed: Changed from findByType to findByTypeAnalyse
     */
    List<Analyse> findByTypeAnalyse(String typeAnalyse);

    /**
     * ✅ Fixed: Changed from findByTypeAnalyseContainingIgnoreCase for partial search
     */
    List<Analyse> findByTypeAnalyseContainingIgnoreCase(String typeAnalyse);

    /**
     * ✅ Fixed: Search by patient ID
     */
    List<Analyse> findByPatientUserId(Long patientUserId);

    /**
     * ✅ Fixed: Corrected date range query using proper field name and LocalDateTime
     */
    @Query("SELECT a FROM Analyse a WHERE a.patientUserId = :patientId AND a.dateAnalyse BETWEEN :start AND :end")
    List<Analyse> findByPatientAndDateRange(@Param("patientId") Long patientId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    /**
     * ✅ Fixed: Search by result content (partial match)
     */
    List<Analyse> findByResultatContainingIgnoreCase(String resultatKeyword);

    /**
     * ✅ Fixed: Get recent analyses for a patient (last N results)
     */
    @Query("SELECT a FROM Analyse a WHERE a.patientUserId = :patientId ORDER BY a.dateAnalyse DESC")
    List<Analyse> findRecentAnalysesByPatient(@Param("patientId") Long patientId);

    /**
     * ✅ Added: Find analyses by dossier and type
     */
    List<Analyse> findByDossierMedicalIdAndTypeAnalyse(Long dossierMedicalId, String typeAnalyse);

    /**
     * ✅ Added: Count analyses by patient
     */
    long countByPatientUserId(Long patientUserId);
}
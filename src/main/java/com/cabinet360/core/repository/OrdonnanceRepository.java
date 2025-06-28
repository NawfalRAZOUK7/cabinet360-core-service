package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Ordonnance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrdonnanceRepository extends JpaRepository<Ordonnance, Long> {

    /**
     * Toutes les ordonnances pour un dossier médical donné.
     */
    List<Ordonnance> findByDossierMedicalId(Long dossierMedicalId);

    /**
     * ✅ Fixed: Changed from findByMedecinPrescripteurUserId to findByMedecinUserId
     */
    List<Ordonnance> findByMedecinUserId(Long medecinUserId);

    /**
     * ✅ Added: Find ordonnances by patient
     */
    List<Ordonnance> findByPatientUserId(Long patientUserId);

    /**
     * ✅ Fixed: Corrected date range query using proper field names and LocalDateTime
     */
    @Query("SELECT o FROM Ordonnance o WHERE o.patientUserId = :patientId AND o.dateOrdonnance BETWEEN :start AND :end")
    List<Ordonnance> findByPatientAndDateRange(@Param("patientId") Long patientId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    /**
     * ✅ Fixed: Search by content instead of medicaments field
     */
    @Query("SELECT o FROM Ordonnance o WHERE LOWER(o.contenu) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Ordonnance> findByContenuContaining(@Param("keyword") String keyword);

    /**
     * ✅ Added: Find ordonnances by medecin and patient
     */
    List<Ordonnance> findByMedecinUserIdAndPatientUserId(Long medecinUserId, Long patientUserId);

    /**
     * ✅ Added: Find ordonnances by medecin and date range
     */
    @Query("SELECT o FROM Ordonnance o WHERE o.medecinUserId = :medecinId AND o.dateOrdonnance BETWEEN :start AND :end")
    List<Ordonnance> findByMedecinAndDateRange(@Param("medecinId") Long medecinUserId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    /**
     * ✅ Added: Find recent ordonnances for a patient
     */
    @Query("SELECT o FROM Ordonnance o WHERE o.patientUserId = :patientId ORDER BY o.dateOrdonnance DESC")
    List<Ordonnance> findRecentOrdonnancesByPatient(@Param("patientId") Long patientUserId);

    /**
     * ✅ Added: Find recent ordonnances by a medecin
     */
    @Query("SELECT o FROM Ordonnance o WHERE o.medecinUserId = :medecinId ORDER BY o.dateOrdonnance DESC")
    List<Ordonnance> findRecentOrdonnancesByMedecin(@Param("medecinId") Long medecinUserId);

    /**
     * ✅ Added: Count ordonnances by patient
     */
    long countByPatientUserId(Long patientUserId);

    /**
     * ✅ Added: Count ordonnances by medecin
     */
    long countByMedecinUserId(Long medecinUserId);

    /**
     * ✅ Added: Find ordonnances by dossier and medecin
     */
    List<Ordonnance> findByDossierMedicalIdAndMedecinUserId(Long dossierMedicalId, Long medecinUserId);

    /**
     * ✅ FIXED: Find ordonnances created today by medecin using proper date range comparison
     * This replaces the problematic DATE() function with a proper LocalDateTime range
     */
    @Query("SELECT o FROM Ordonnance o WHERE o.medecinUserId = :medecinId " +
            "AND o.dateOrdonnance >= :startOfDay AND o.dateOrdonnance < :endOfDay")
    List<Ordonnance> findTodayOrdonnancesByMedecin(@Param("medecinId") Long medecinUserId,
                                                   @Param("startOfDay") LocalDateTime startOfDay,
                                                   @Param("endOfDay") LocalDateTime endOfDay);
}
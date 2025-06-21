package com.cabinet360.core.repository;

import com.cabinet360.core.entity.DossierMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {

    /**
     * ✅ Fixed: Find by patient (unique constraint ensures one dossier per patient)
     */
    Optional<DossierMedical> findByPatientUserId(Long patientUserId);

    /**
     * ✅ Fixed: Changed from findByMedecinPrincipalUserId to findByMedecinUserId
     */
    List<DossierMedical> findByMedecinUserId(Long medecinUserId);

    /**
     * ✅ Added: Find dossiers by status
     */
    List<DossierMedical> findByStatut(String statut);

    /**
     * ✅ Fixed: Removed non-existent field references, created proper search
     */
    @Query("SELECT d FROM DossierMedical d WHERE " +
            "(:patientId IS NULL OR d.patientUserId = :patientId) AND " +
            "(:medecinId IS NULL OR d.medecinUserId = :medecinId) AND " +
            "(:keyword IS NULL OR LOWER(d.resumeHistorique) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.commentaires) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DossierMedical> searchDossiers(@Param("patientId") Long patientId,
                                        @Param("medecinId") Long medecinId,
                                        @Param("keyword") String keyword);

    /**
     * ✅ Fixed: Proper implementation using medecinsAutorises relationship
     */
    @Query("SELECT d FROM DossierMedical d JOIN d.medecinsAutorises m WHERE m.doctorUserId = :doctorUserId")
    List<DossierMedical> findDossiersAccessibleByDoctor(@Param("doctorUserId") Long doctorUserId);

    /**
     * ✅ Added: Find dossiers created within date range
     */
    @Query("SELECT d FROM DossierMedical d WHERE d.createdAt BETWEEN :start AND :end")
    List<DossierMedical> findByCreatedAtBetween(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    /**
     * ✅ Added: Find recently accessed dossiers
     */
    @Query("SELECT d FROM DossierMedical d WHERE d.lastAccessedAt IS NOT NULL ORDER BY d.lastAccessedAt DESC")
    List<DossierMedical> findRecentlyAccessed();

    /**
     * ✅ Added: Find dossiers last accessed by a user
     */
    List<DossierMedical> findByLastAccessedBy(Long userId);

    /**
     * ✅ Added: Count dossiers by medecin
     */
    long countByMedecinUserId(Long medecinUserId);

    /**
     * ✅ Added: Count dossiers by status
     */
    long countByStatut(String statut);

    /**
     * ✅ Added: Search in resume historique
     */
    List<DossierMedical> findByResumeHistoriqueContainingIgnoreCase(String keyword);

    /**
     * ✅ Added: Find dossiers needing attention (no recent access)
     */
    @Query("SELECT d FROM DossierMedical d WHERE d.lastAccessedAt < :cutoffDate OR d.lastAccessedAt IS NULL")
    List<DossierMedical> findDossiersNeedingAttention(@Param("cutoffDate") LocalDateTime cutoffDate);
}
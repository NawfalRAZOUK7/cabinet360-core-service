package com.cabinet360.core.repository;

import com.cabinet360.core.entity.NoteMedicale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteMedicaleRepository extends JpaRepository<NoteMedicale, Long> {

    /**
     * Toutes les notes pour un dossier médical.
     */
    List<NoteMedicale> findByDossierMedicalId(Long dossierMedicalId);

    /**
     * ✅ Fixed: Changed from findByAuteurUserId to findByMedecinUserId
     */
    List<NoteMedicale> findByMedecinUserId(Long medecinUserId);

    /**
     * ✅ Fixed: Changed from findByDateCreationRange to findByDateNoteRange
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.dateNote BETWEEN :start AND :end")
    List<NoteMedicale> findByDateNoteRange(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    /**
     * ✅ Fixed: Changed from findByTexteContainingIgnoreCase to findByContenuContainingIgnoreCase
     */
    List<NoteMedicale> findByContenuContainingIgnoreCase(String keyword);

    /**
     * ✅ Added: Find notes by dossier and medecin
     */
    List<NoteMedicale> findByDossierMedicalIdAndMedecinUserId(Long dossierMedicalId, Long medecinUserId);

    /**
     * ✅ Added: Find notes by medecin within date range
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.medecinUserId = :medecinId AND n.dateNote BETWEEN :start AND :end")
    List<NoteMedicale> findByMedecinAndDateRange(@Param("medecinId") Long medecinUserId,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    /**
     * ✅ Added: Find recent notes for a dossier
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.dossierMedical.id = :dossierId ORDER BY n.dateNote DESC")
    List<NoteMedicale> findRecentNotesByDossier(@Param("dossierId") Long dossierMedicalId);

    /**
     * ✅ Added: Find recent notes by a medecin
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.medecinUserId = :medecinId ORDER BY n.dateNote DESC")
    List<NoteMedicale> findRecentNotesByMedecin(@Param("medecinId") Long medecinUserId);

    /**
     * ✅ FIXED: Corrected today's notes query - using LocalDateTime comparison instead of DATE() function
     * The previous query used DATE() function which caused type mismatch between java.lang.Object and java.sql.Date
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.medecinUserId = :medecinId " +
            "AND n.dateNote >= :startOfDay AND n.dateNote < :endOfDay")
    List<NoteMedicale> findTodayNotesByMedecin(@Param("medecinId") Long medecinUserId,
                                               @Param("startOfDay") LocalDateTime startOfDay,
                                               @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * ✅ Added: Count notes by medecin
     */
    long countByMedecinUserId(Long medecinUserId);

    /**
     * ✅ Added: Count notes by dossier medical
     */
    long countByDossierMedicalId(Long dossierMedicalId);

    /**
     * ✅ Added: Find notes by patient (through dossier medical)
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.dossierMedical.patientUserId = :patientId")
    List<NoteMedicale> findByPatientUserId(@Param("patientId") Long patientUserId);

    /**
     * ✅ Added: Search notes by content with minimum length
     */
    @Query("SELECT n FROM NoteMedicale n WHERE LENGTH(n.contenu) >= :minLength AND LOWER(n.contenu) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<NoteMedicale> findDetailedNotesByKeyword(@Param("keyword") String keyword, @Param("minLength") int minLength);

    /**
     * ✅ Added: Alternative today's notes query using native SQL (if needed)
     * This can be used as a fallback if the HQL version above still has issues
     */
    @Query(value = "SELECT * FROM note_medicale n WHERE n.medecin_user_id = :medecinId " +
            "AND DATE(n.date_note) = CURRENT_DATE", nativeQuery = true)
    List<NoteMedicale> findTodayNotesByMedecinNative(@Param("medecinId") Long medecinUserId);

    /**
     * ✅ Added: Find notes created in the last N hours
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.medecinUserId = :medecinId " +
            "AND n.dateNote >= :sinceTime ORDER BY n.dateNote DESC")
    List<NoteMedicale> findRecentNotesByMedecinSince(@Param("medecinId") Long medecinUserId,
                                                     @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * ✅ Added: Find notes created this week by medecin
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.medecinUserId = :medecinId " +
            "AND n.dateNote >= :startOfWeek AND n.dateNote < :endOfWeek")
    List<NoteMedicale> findThisWeekNotesByMedecin(@Param("medecinId") Long medecinUserId,
                                                  @Param("startOfWeek") LocalDateTime startOfWeek,
                                                  @Param("endOfWeek") LocalDateTime endOfWeek);

    /**
     * ✅ Added: Find notes created this month by medecin
     */
    @Query("SELECT n FROM NoteMedicale n WHERE n.medecinUserId = :medecinId " +
            "AND n.dateNote >= :startOfMonth AND n.dateNote < :endOfMonth")
    List<NoteMedicale> findThisMonthNotesByMedecin(@Param("medecinId") Long medecinUserId,
                                                   @Param("startOfMonth") LocalDateTime startOfMonth,
                                                   @Param("endOfMonth") LocalDateTime endOfMonth);
}
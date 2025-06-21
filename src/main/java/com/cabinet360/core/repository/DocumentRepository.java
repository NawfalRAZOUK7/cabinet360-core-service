package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Tous les documents rattachés à un dossier médical.
     */
    List<Document> findByDossierMedicalId(Long dossierMedicalId);

    /**
     * ✅ Fixed: Changed from findByType to findByTypeDocument
     */
    List<Document> findByTypeDocument(String typeDocument);

    /**
     * ✅ Fixed: Changed from findByUploaderUserId to findByPatientUserId
     */
    List<Document> findByPatientUserId(Long patientUserId);

    /**
     * ✅ Added: Search documents by name (partial match)
     */
    List<Document> findByNomContainingIgnoreCase(String nom);

    /**
     * ✅ Added: Search documents by type (partial match)
     */
    List<Document> findByTypeDocumentContainingIgnoreCase(String typeDocument);

    /**
     * ✅ Added: Find documents by dossier and type
     */
    List<Document> findByDossierMedicalIdAndTypeDocument(Long dossierMedicalId, String typeDocument);

    /**
     * ✅ Added: Find documents uploaded within date range
     */
    @Query("SELECT d FROM Document d WHERE d.dateUpload BETWEEN :startDate AND :endDate")
    List<Document> findByDateUploadBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * ✅ Added: Find documents by patient and date range
     */
    @Query("SELECT d FROM Document d WHERE d.patientUserId = :patientId AND d.dateUpload BETWEEN :startDate AND :endDate")
    List<Document> findByPatientAndDateRange(@Param("patientId") Long patientId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * ✅ Added: Find recent documents for a patient
     */
    @Query("SELECT d FROM Document d WHERE d.patientUserId = :patientId ORDER BY d.dateUpload DESC")
    List<Document> findRecentDocumentsByPatient(@Param("patientId") Long patientId);

    /**
     * ✅ Added: Count documents by patient
     */
    long countByPatientUserId(Long patientUserId);

    /**
     * ✅ Added: Count documents by type
     */
    long countByTypeDocument(String typeDocument);

    /**
     * ✅ Added: Find documents by URL (for duplicate checking)
     */
    List<Document> findByUrl(String url);
}
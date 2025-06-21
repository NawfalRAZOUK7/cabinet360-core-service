package com.cabinet360.core.service;

import com.cabinet360.core.dto.DossierMedicalDto;
import com.cabinet360.core.entity.*;
import com.cabinet360.core.exception.*;
import com.cabinet360.core.mapper.DossierMedicalMapper;
import com.cabinet360.core.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing medical records (DossierMedical).
 * Central service that coordinates all medical data for patients.
 */
@Service
@Transactional(readOnly = true)
public class DossierMedicalService {

    private static final Logger logger = LoggerFactory.getLogger(DossierMedicalService.class);

    private final DossierMedicalRepository dossierMedicalRepository;
    private final MedecinRepository medecinRepository;
    private final AnalyseRepository analyseRepository;
    private final OrdonnanceRepository ordonnanceRepository;
    private final DocumentRepository documentRepository;
    private final NoteMedicaleRepository noteMedicaleRepository;
    private final DossierMedicalMapper dossierMedicalMapper;

    @Autowired
    public DossierMedicalService(DossierMedicalRepository dossierMedicalRepository,
                                 MedecinRepository medecinRepository,
                                 AnalyseRepository analyseRepository,
                                 OrdonnanceRepository ordonnanceRepository,
                                 DocumentRepository documentRepository,
                                 NoteMedicaleRepository noteMedicaleRepository,
                                 DossierMedicalMapper dossierMedicalMapper) {
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.medecinRepository = medecinRepository;
        this.analyseRepository = analyseRepository;
        this.ordonnanceRepository = ordonnanceRepository;
        this.documentRepository = documentRepository;
        this.noteMedicaleRepository = noteMedicaleRepository;
        this.dossierMedicalMapper = dossierMedicalMapper;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Creates a new medical record for a patient.
     * Enforces one dossier per patient rule.
     */
    @Transactional
    public DossierMedicalDto createDossierMedical(DossierMedicalDto dossierMedicalDto) {
        logger.info("Creating new medical record for patient: {}", dossierMedicalDto.getPatientUserId());

        // Check if patient already has a dossier medical
        Optional<DossierMedical> existingDossier = dossierMedicalRepository
                .findByPatientUserId(dossierMedicalDto.getPatientUserId());

        if (existingDossier.isPresent()) {
            throw new DossierMedicalAlreadyExistsException(
                    "Patient " + dossierMedicalDto.getPatientUserId() + " already has a medical record");
        }

        // Set default values
        if (dossierMedicalDto.getCreatedAt() == null) {
            dossierMedicalDto.setCreatedAt(LocalDateTime.now());
        }
        if (dossierMedicalDto.getUpdatedAt() == null) {
            dossierMedicalDto.setUpdatedAt(LocalDateTime.now());
        }
        if (dossierMedicalDto.getStatut() == null) {
            dossierMedicalDto.setStatut("ACTIF");
        }

        // Validate medecin exists if provided
        if (dossierMedicalDto.getMedecinUserId() != null) {
            validateMedecinExists(dossierMedicalDto.getMedecinUserId());
        }

        // Convert to entity and save
        DossierMedical dossierMedical = dossierMedicalMapper.toEntity(dossierMedicalDto);
        DossierMedical savedDossier = dossierMedicalRepository.save(dossierMedical);

        logger.info("Created medical record with ID: {} for patient: {}",
                savedDossier.getId(), savedDossier.getPatientUserId());
        return dossierMedicalMapper.toDto(savedDossier);
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Finds a medical record by its ID.
     */
    public DossierMedicalDto findById(Long id) {
        logger.debug("Finding medical record by ID: {}", id);

        DossierMedical dossierMedical = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + id));

        // Update last accessed info
        updateLastAccessed(dossierMedical, null);

        return dossierMedicalMapper.toDto(dossierMedical);
    }

    /**
     * Finds a medical record by patient ID.
     */
    public DossierMedicalDto findByPatientUserId(Long patientUserId) {
        logger.debug("Finding medical record for patient: {}", patientUserId);

        DossierMedical dossierMedical = dossierMedicalRepository.findByPatientUserId(patientUserId)
                .orElseThrow(() -> new DossierMedicalNotFoundException(
                        "No medical record found for patient: " + patientUserId));

        // Update last accessed info
        updateLastAccessed(dossierMedical, null);

        return dossierMedicalMapper.toDto(dossierMedical);
    }

    /**
     * Gets all medical records with pagination.
     */
    public Page<DossierMedicalDto> findAll(int page, int size, String sortBy, String sortDirection) {
        logger.debug("Finding all medical records - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return dossierMedicalRepository.findAll(pageable)
                .map(dossierMedicalMapper::toDto);
    }

    /**
     * Gets all medical records assigned to a doctor.
     */
    public List<DossierMedicalDto> findByMedecin(Long medecinUserId) {
        logger.debug("Finding medical records for doctor: {}", medecinUserId);

        return dossierMedicalRepository.findByMedecinUserId(medecinUserId)
                .stream()
                .map(dossierMedicalMapper::toDto)
                .toList();
    }

    /**
     * Gets medical records accessible to a doctor (including shared ones).
     */
    public List<DossierMedicalDto> findAccessibleByDoctor(Long doctorUserId) {
        logger.debug("Finding medical records accessible by doctor: {}", doctorUserId);

        return dossierMedicalRepository.findDossiersAccessibleByDoctor(doctorUserId)
                .stream()
                .map(dossierMedicalMapper::toDto)
                .toList();
    }

    /**
     * Gets medical records by status.
     */
    public List<DossierMedicalDto> findByStatus(String statut) {
        logger.debug("Finding medical records with status: {}", statut);

        return dossierMedicalRepository.findByStatut(statut)
                .stream()
                .map(dossierMedicalMapper::toDto)
                .toList();
    }

    /**
     * Searches medical records with multiple criteria.
     */
    public List<DossierMedicalDto> searchDossiers(Long patientId, Long medecinId, String keyword) {
        logger.debug("Searching medical records - patient: {}, medecin: {}, keyword: {}",
                patientId, medecinId, keyword);

        return dossierMedicalRepository.searchDossiers(patientId, medecinId, keyword)
                .stream()
                .map(dossierMedicalMapper::toDto)
                .toList();
    }

    /**
     * Gets medical records created within a date range.
     */
    public List<DossierMedicalDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding medical records created between {} and {}", startDate, endDate);

        return dossierMedicalRepository.findByCreatedAtBetween(startDate, endDate)
                .stream()
                .map(dossierMedicalMapper::toDto)
                .toList();
    }

    /**
     * Gets recently accessed medical records.
     */
    public List<DossierMedicalDto> findRecentlyAccessed(int limit) {
        logger.debug("Finding {} recently accessed medical records", limit);

        return dossierMedicalRepository.findRecentlyAccessed()
                .stream()
                .limit(limit)
                .map(dossierMedicalMapper::toDto)
                .toList();
    }

    /**
     * Gets medical records that need attention (not accessed recently).
     */
    public List<DossierMedicalDto> findDossiersNeedingAttention(int daysWithoutAccess) {
        logger.debug("Finding medical records not accessed for {} days", daysWithoutAccess);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysWithoutAccess);
        return dossierMedicalRepository.findDossiersNeedingAttention(cutoffDate)
                .stream()
                .map(dossierMedicalMapper::toDto)
                .toList();
    }

    /**
     * Gets complete medical record with all related data.
     */
    public CompletePatientRecord getCompletePatientRecord(Long patientUserId) {
        logger.debug("Getting complete medical record for patient: {}", patientUserId);

        DossierMedical dossier = dossierMedicalRepository.findByPatientUserId(patientUserId)
                .orElseThrow(() -> new DossierMedicalNotFoundException(
                        "No medical record found for patient: " + patientUserId));

        // Update last accessed
        updateLastAccessed(dossier, null);

        // Get all related data
        List<Analyse> analyses = analyseRepository.findByDossierMedicalId(dossier.getId());
        List<Ordonnance> ordonnances = ordonnanceRepository.findByDossierMedicalId(dossier.getId());
        List<Document> documents = documentRepository.findByDossierMedicalId(dossier.getId());
        List<NoteMedicale> notes = noteMedicaleRepository.findByDossierMedicalId(dossier.getId());

        return new CompletePatientRecord(
                dossierMedicalMapper.toDto(dossier),
                analyses,
                ordonnances,
                documents,
                notes
        );
    }

    /**
     * Gets count statistics for medical records.
     */
    public DossierStats getDossierStats() {
        logger.debug("Getting medical record statistics");

        long totalDossiers = dossierMedicalRepository.count();
        long activeDossiers = dossierMedicalRepository.countByStatut("ACTIF");
        long archivedDossiers = dossierMedicalRepository.countByStatut("ARCHIVE");

        return new DossierStats(totalDossiers, activeDossiers, archivedDossiers);
    }

    /**
     * Gets count by doctor.
     */
    public long countByMedecin(Long medecinUserId) {
        logger.debug("Counting medical records for doctor: {}", medecinUserId);
        return dossierMedicalRepository.countByMedecinUserId(medecinUserId);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Updates an existing medical record.
     */
    @Transactional
    public DossierMedicalDto updateDossierMedical(Long id, DossierMedicalDto dossierMedicalDto) {
        logger.info("Updating medical record with ID: {}", id);

        DossierMedical existingDossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + id));

        // Validate medecin if changed
        if (dossierMedicalDto.getMedecinUserId() != null) {
            validateMedecinExists(dossierMedicalDto.getMedecinUserId());
        }

        // Set updated timestamp
        dossierMedicalDto.setUpdatedAt(LocalDateTime.now());

        // Update entity using mapper
        dossierMedicalMapper.updateEntityFromDto(dossierMedicalDto, existingDossier);

        DossierMedical updatedDossier = dossierMedicalRepository.save(existingDossier);

        logger.info("Updated medical record with ID: {}", updatedDossier.getId());
        return dossierMedicalMapper.toDto(updatedDossier);
    }

    /**
     * Partially updates a medical record.
     */
    @Transactional
    public DossierMedicalDto partialUpdate(Long id, DossierMedicalDto partialDto) {
        logger.info("Partially updating medical record with ID: {}", id);

        DossierMedical existingDossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + id));

        // Only update non-null fields
        if (partialDto.getMedecinUserId() != null) {
            validateMedecinExists(partialDto.getMedecinUserId());
            existingDossier.setMedecinUserId(partialDto.getMedecinUserId());
        }
        if (partialDto.getResumeHistorique() != null) {
            existingDossier.setResumeHistorique(partialDto.getResumeHistorique());
        }
        if (partialDto.getStatut() != null) {
            existingDossier.setStatut(partialDto.getStatut());
        }
        if (partialDto.getCommentaires() != null) {
            existingDossier.setCommentaires(partialDto.getCommentaires());
        }

        // Always update timestamp
        existingDossier.setUpdatedAt(LocalDateTime.now());

        DossierMedical updatedDossier = dossierMedicalRepository.save(existingDossier);

        logger.info("Partially updated medical record with ID: {}", updatedDossier.getId());
        return dossierMedicalMapper.toDto(updatedDossier);
    }

    /**
     * Updates medical record status.
     */
    @Transactional
    public DossierMedicalDto updateStatus(Long id, String newStatus) {
        logger.info("Updating status for medical record ID: {} to: {}", id, newStatus);

        DossierMedical dossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + id));

        dossier.setStatut(newStatus);
        dossier.setUpdatedAt(LocalDateTime.now());

        DossierMedical updatedDossier = dossierMedicalRepository.save(dossier);

        logger.info("Updated status for medical record ID: {}", updatedDossier.getId());
        return dossierMedicalMapper.toDto(updatedDossier);
    }

    /**
     * Assigns a doctor to a medical record.
     */
    @Transactional
    public DossierMedicalDto assignMedecin(Long dossierId, Long medecinUserId) {
        logger.info("Assigning doctor {} to medical record {}", medecinUserId, dossierId);

        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + dossierId));

        validateMedecinExists(medecinUserId);

        dossier.setMedecinUserId(medecinUserId);
        dossier.setUpdatedAt(LocalDateTime.now());

        DossierMedical updatedDossier = dossierMedicalRepository.save(dossier);

        logger.info("Assigned doctor {} to medical record {}", medecinUserId, dossierId);
        return dossierMedicalMapper.toDto(updatedDossier);
    }

    /**
     * Adds a doctor to authorized access list.
     */
    @Transactional
    public DossierMedicalDto addAuthorizedMedecin(Long dossierId, Long doctorUserId) {
        logger.info("Adding authorized doctor {} to medical record {}", doctorUserId, dossierId);

        DossierMedical dossier = dossierMedicalRepository.findById(dossierId)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + dossierId));

        Medecin medecin = medecinRepository.findByDoctorUserId(doctorUserId)
                .orElseThrow(() -> new RuntimeException("Medecin not found with doctorUserId: " + doctorUserId));

        if (dossier.getMedecinsAutorises() != null && !dossier.getMedecinsAutorises().contains(medecin)) {
            dossier.getMedecinsAutorises().add(medecin);
            dossier.setUpdatedAt(LocalDateTime.now());

            DossierMedical updatedDossier = dossierMedicalRepository.save(dossier);

            logger.info("Added authorized doctor {} to medical record {}", doctorUserId, dossierId);
            return dossierMedicalMapper.toDto(updatedDossier);
        }

        return dossierMedicalMapper.toDto(dossier);
    }

    /**
     * Updates historical summary.
     */
    @Transactional
    public DossierMedicalDto updateHistoricalSummary(Long id, String historicalSummary) {
        logger.info("Updating historical summary for medical record ID: {}", id);

        DossierMedical dossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + id));

        dossier.setResumeHistorique(historicalSummary);
        dossier.setUpdatedAt(LocalDateTime.now());

        DossierMedical updatedDossier = dossierMedicalRepository.save(dossier);

        logger.info("Updated historical summary for medical record ID: {}", updatedDossier.getId());
        return dossierMedicalMapper.toDto(updatedDossier);
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Archives a medical record (soft delete).
     */
    @Transactional
    public DossierMedicalDto archiveDossierMedical(Long id) {
        logger.info("Archiving medical record with ID: {}", id);

        return updateStatus(id, "ARCHIVE");
    }

    /**
     * Permanently deletes a medical record and all related data.
     * ⚠️ Use with extreme caution!
     */
    @Transactional
    public void deleteDossierMedicalPermanently(Long id) {
        logger.warn("PERMANENTLY deleting medical record with ID: {}", id);

        DossierMedical dossier = dossierMedicalRepository.findById(id)
                .orElseThrow(() -> new DossierMedicalNotFoundException("Medical record not found with ID: " + id));

        // Delete all related data first
        analyseRepository.deleteAll(analyseRepository.findByDossierMedicalId(id));
        ordonnanceRepository.deleteAll(ordonnanceRepository.findByDossierMedicalId(id));
        documentRepository.deleteAll(documentRepository.findByDossierMedicalId(id));
        noteMedicaleRepository.deleteAll(noteMedicaleRepository.findByDossierMedicalId(id));

        // Delete the dossier itself
        dossierMedicalRepository.delete(dossier);

        logger.warn("PERMANENTLY deleted medical record with ID: {}", id);
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Validates that a medecin exists.
     */
    private void validateMedecinExists(Long medecinUserId) {
        if (medecinUserId != null && !medecinRepository.findByDoctorUserId(medecinUserId).isPresent()) {
            throw new RuntimeException("Medecin not found with doctorUserId: " + medecinUserId);
        }
    }

    /**
     * Updates last accessed information.
     */
    private void updateLastAccessed(DossierMedical dossier, Long accessedBy) {
        dossier.setLastAccessedAt(LocalDateTime.now());
        if (accessedBy != null) {
            dossier.setLastAccessedBy(accessedBy);
        }
        dossierMedicalRepository.save(dossier);
    }

    /**
     * Checks if a medical record exists.
     */
    public boolean existsById(Long id) {
        return dossierMedicalRepository.existsById(id);
    }

    /**
     * Checks if a patient has a medical record.
     */
    public boolean existsByPatientUserId(Long patientUserId) {
        return dossierMedicalRepository.findByPatientUserId(patientUserId).isPresent();
    }

    /**
     * Gets medical record by ID or returns empty.
     */
    public Optional<DossierMedicalDto> findByIdOptional(Long id) {
        return dossierMedicalRepository.findById(id)
                .map(dossierMedicalMapper::toDto);
    }

    /**
     * Validates access permissions for a doctor.
     */
    public boolean canDoctorAccessDossier(Long doctorUserId, Long dossierId) {
        DossierMedical dossier = dossierMedicalRepository.findById(dossierId).orElse(null);
        if (dossier == null) return false;

        // Check if doctor is the main assigned doctor
        if (doctorUserId.equals(dossier.getMedecinUserId())) {
            return true;
        }

        // Check if doctor is in authorized list
        return dossier.getMedecinsAutorises() != null &&
                dossier.getMedecinsAutorises().stream()
                        .anyMatch(medecin -> doctorUserId.equals(medecin.getDoctorUserId()));
    }

    // ========================================
    // INNER CLASSES
    // ========================================

    /**
     * Complete patient record with all related medical data.
     */
    public static class CompletePatientRecord {
        private final DossierMedicalDto dossierMedical;
        private final List<Analyse> analyses;
        private final List<Ordonnance> ordonnances;
        private final List<Document> documents;
        private final List<NoteMedicale> notes;

        public CompletePatientRecord(DossierMedicalDto dossierMedical,
                                     List<Analyse> analyses,
                                     List<Ordonnance> ordonnances,
                                     List<Document> documents,
                                     List<NoteMedicale> notes) {
            this.dossierMedical = dossierMedical;
            this.analyses = analyses;
            this.ordonnances = ordonnances;
            this.documents = documents;
            this.notes = notes;
        }

        public DossierMedicalDto getDossierMedical() { return dossierMedical; }
        public List<Analyse> getAnalyses() { return analyses; }
        public List<Ordonnance> getOrdonnances() { return ordonnances; }
        public List<Document> getDocuments() { return documents; }
        public List<NoteMedicale> getNotes() { return notes; }

        @Override
        public String toString() {
            return "CompletePatientRecord{" +
                    "dossierMedical=" + dossierMedical.getId() +
                    ", analyses=" + analyses.size() +
                    ", ordonnances=" + ordonnances.size() +
                    ", documents=" + documents.size() +
                    ", notes=" + notes.size() +
                    '}';
        }
    }

    /**
     * Statistics for medical records.
     */
    public static class DossierStats {
        private final long totalDossiers;
        private final long activeDossiers;
        private final long archivedDossiers;

        public DossierStats(long totalDossiers, long activeDossiers, long archivedDossiers) {
            this.totalDossiers = totalDossiers;
            this.activeDossiers = activeDossiers;
            this.archivedDossiers = archivedDossiers;
        }

        public long getTotalDossiers() { return totalDossiers; }
        public long getActiveDossiers() { return activeDossiers; }
        public long getArchivedDossiers() { return archivedDossiers; }

        @Override
        public String toString() {
            return "DossierStats{" +
                    "totalDossiers=" + totalDossiers +
                    ", activeDossiers=" + activeDossiers +
                    ", archivedDossiers=" + archivedDossiers +
                    '}';
        }
    }
}
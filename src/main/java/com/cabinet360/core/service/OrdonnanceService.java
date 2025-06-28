package com.cabinet360.core.service;

import com.cabinet360.core.dto.OrdonnanceDto;
import com.cabinet360.core.entity.Ordonnance;
import com.cabinet360.core.entity.DossierMedical;
import com.cabinet360.core.exception.OrdonnanceNotFoundException;
import com.cabinet360.core.exception.DossierMedicalNotFoundException;
import com.cabinet360.core.mapper.OrdonnanceMapper;
import com.cabinet360.core.repository.OrdonnanceRepository;
import com.cabinet360.core.repository.DossierMedicalRepository;
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
import java.util.regex.Pattern;

/**
 * Service for managing medical prescriptions (Ordonnance).
 * Provides CRUD operations with proper transaction management and prescription validation.
 */
@Service
@Transactional(readOnly = true)
public class OrdonnanceService {

    private static final Logger logger = LoggerFactory.getLogger(OrdonnanceService.class);

    // Pattern for basic medication validation (can be enhanced)
    private static final Pattern MEDICATION_PATTERN = Pattern.compile(".*\\b(mg|g|ml|comprimé|gélule|sirop)\\b.*", Pattern.CASE_INSENSITIVE);

    private final OrdonnanceRepository ordonnanceRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final OrdonnanceMapper ordonnanceMapper;

    @Autowired
    public OrdonnanceService(OrdonnanceRepository ordonnanceRepository,
                             DossierMedicalRepository dossierMedicalRepository,
                             OrdonnanceMapper ordonnanceMapper) {
        this.ordonnanceRepository = ordonnanceRepository;
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.ordonnanceMapper = ordonnanceMapper;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Creates a new prescription for a patient.
     */
    @Transactional
    public OrdonnanceDto createOrdonnance(OrdonnanceDto ordonnanceDto) {
        logger.info("Creating new prescription for patient: {} by doctor: {}",
                ordonnanceDto.getPatientUserId(), ordonnanceDto.getMedecinUserId());

        // Validate dossier medical exists
        validateDossierMedicalExists(ordonnanceDto.getDossierMedicalId());

        // Set prescription date if not provided
        if (ordonnanceDto.getDateOrdonnance() == null) {
            ordonnanceDto.setDateOrdonnance(LocalDateTime.now());
        }

        // Validate prescription content
        validatePrescriptionContent(ordonnanceDto.getContenu());

        // Convert to entity and save
        Ordonnance ordonnance = ordonnanceMapper.toEntity(ordonnanceDto);
        Ordonnance savedOrdonnance = ordonnanceRepository.save(ordonnance);

        logger.info("Created prescription with ID: {}", savedOrdonnance.getId());
        return ordonnanceMapper.toDto(savedOrdonnance);
    }

    /**
     * Creates multiple prescriptions in batch.
     */
    @Transactional
    public List<OrdonnanceDto> createOrdonnancesBatch(List<OrdonnanceDto> ordonnanceDtos) {
        logger.info("Creating {} prescriptions in batch", ordonnanceDtos.size());

        // Validate all dossiers exist
        ordonnanceDtos.forEach(dto -> validateDossierMedicalExists(dto.getDossierMedicalId()));

        // Set prescription dates and validate content
        ordonnanceDtos.forEach(dto -> {
            if (dto.getDateOrdonnance() == null) {
                dto.setDateOrdonnance(LocalDateTime.now());
            }
            validatePrescriptionContent(dto.getContenu());
        });

        // Convert and save all
        List<Ordonnance> ordonnances = ordonnanceDtos.stream()
                .map(ordonnanceMapper::toEntity)
                .toList();

        List<Ordonnance> savedOrdonnances = ordonnanceRepository.saveAll(ordonnances);

        logger.info("Created {} prescriptions successfully", savedOrdonnances.size());
        return savedOrdonnances.stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Finds a prescription by its ID.
     */
    public OrdonnanceDto findById(Long id) {
        logger.debug("Finding prescription by ID: {}", id);

        Ordonnance ordonnance = ordonnanceRepository.findById(id)
                .orElseThrow(() -> new OrdonnanceNotFoundException("Prescription not found with ID: " + id));

        return ordonnanceMapper.toDto(ordonnance);
    }

    /**
     * Gets all prescriptions with pagination.
     */
    public Page<OrdonnanceDto> findAll(int page, int size, String sortBy, String sortDirection) {
        logger.debug("Finding all prescriptions - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return ordonnanceRepository.findAll(pageable)
                .map(ordonnanceMapper::toDto);
    }

    /**
     * Gets all prescriptions for a specific medical record.
     */
    public List<OrdonnanceDto> findByDossierMedical(Long dossierMedicalId) {
        logger.debug("Finding prescriptions for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        return ordonnanceRepository.findByDossierMedicalId(dossierMedicalId)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets all prescriptions for a specific patient.
     */
    public List<OrdonnanceDto> findByPatient(Long patientUserId) {
        logger.debug("Finding prescriptions for patient: {}", patientUserId);

        return ordonnanceRepository.findByPatientUserId(patientUserId)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets all prescriptions created by a specific doctor.
     */
    public List<OrdonnanceDto> findByMedecin(Long medecinUserId) {
        logger.debug("Finding prescriptions by doctor: {}", medecinUserId);

        return ordonnanceRepository.findByMedecinUserId(medecinUserId)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets prescriptions for a patient within a date range.
     */
    public List<OrdonnanceDto> findByPatientAndDateRange(Long patientUserId,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        logger.debug("Finding prescriptions for patient {} between {} and {}",
                patientUserId, startDate, endDate);

        return ordonnanceRepository.findByPatientAndDateRange(patientUserId, startDate, endDate)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets prescriptions by doctor within a date range.
     */
    public List<OrdonnanceDto> findByMedecinAndDateRange(Long medecinUserId,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        logger.debug("Finding prescriptions by doctor {} between {} and {}",
                medecinUserId, startDate, endDate);

        return ordonnanceRepository.findByMedecinAndDateRange(medecinUserId, startDate, endDate)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Searches prescriptions by content.
     */
    public List<OrdonnanceDto> searchByContent(String keyword) {
        logger.debug("Searching prescriptions by content keyword: {}", keyword);

        return ordonnanceRepository.findByContenuContaining(keyword)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets prescriptions for a specific doctor-patient combination.
     */
    public List<OrdonnanceDto> findByMedecinAndPatient(Long medecinUserId, Long patientUserId) {
        logger.debug("Finding prescriptions by doctor {} for patient {}", medecinUserId, patientUserId);

        return ordonnanceRepository.findByMedecinUserIdAndPatientUserId(medecinUserId, patientUserId)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets recent prescriptions for a patient.
     */
    public List<OrdonnanceDto> findRecentPrescriptionsByPatient(Long patientUserId, int limit) {
        logger.debug("Finding {} recent prescriptions for patient: {}", limit, patientUserId);

        return ordonnanceRepository.findRecentOrdonnancesByPatient(patientUserId)
                .stream()
                .limit(limit)
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets recent prescriptions by a doctor.
     */
    public List<OrdonnanceDto> findRecentPrescriptionsByMedecin(Long medecinUserId, int limit) {
        logger.debug("Finding {} recent prescriptions by doctor: {}", limit, medecinUserId);

        return ordonnanceRepository.findRecentOrdonnancesByMedecin(medecinUserId)
                .stream()
                .limit(limit)
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets today's prescriptions by a doctor.
     * ✅ FIXED: Updated to work with the new repository method signature
     */
    public List<OrdonnanceDto> findTodayPrescriptionsByMedecin(Long medecinUserId) {
        logger.debug("Finding today's prescriptions by doctor: {}", medecinUserId);

        // Get today's date range (from start of day to start of next day)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return ordonnanceRepository.findTodayOrdonnancesByMedecin(medecinUserId, startOfDay, endOfDay)
                .stream()
                .map(ordonnanceMapper::toDto)
                .toList();
    }

    /**
     * Gets prescription count for a patient.
     */
    public long countByPatient(Long patientUserId) {
        logger.debug("Counting prescriptions for patient: {}", patientUserId);
        return ordonnanceRepository.countByPatientUserId(patientUserId);
    }

    /**
     * Gets prescription count by doctor.
     */
    public long countByMedecin(Long medecinUserId) {
        logger.debug("Counting prescriptions by doctor: {}", medecinUserId);
        return ordonnanceRepository.countByMedecinUserId(medecinUserId);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Updates an existing prescription.
     */
    @Transactional
    public OrdonnanceDto updateOrdonnance(Long id, OrdonnanceDto ordonnanceDto) {
        logger.info("Updating prescription with ID: {}", id);

        Ordonnance existingOrdonnance = ordonnanceRepository.findById(id)
                .orElseThrow(() -> new OrdonnanceNotFoundException("Prescription not found with ID: " + id));

        // Validate dossier medical if changed
        if (ordonnanceDto.getDossierMedicalId() != null &&
                !ordonnanceDto.getDossierMedicalId().equals(existingOrdonnance.getDossierMedical().getId())) {
            validateDossierMedicalExists(ordonnanceDto.getDossierMedicalId());
        }

        // Validate prescription content if changed
        if (ordonnanceDto.getContenu() != null) {
            validatePrescriptionContent(ordonnanceDto.getContenu());
        }

        // Update entity using mapper
        ordonnanceMapper.updateEntityFromDto(ordonnanceDto, existingOrdonnance);

        Ordonnance updatedOrdonnance = ordonnanceRepository.save(existingOrdonnance);

        logger.info("Updated prescription with ID: {}", updatedOrdonnance.getId());
        return ordonnanceMapper.toDto(updatedOrdonnance);
    }

    /**
     * Partially updates a prescription.
     */
    @Transactional
    public OrdonnanceDto partialUpdate(Long id, OrdonnanceDto partialDto) {
        logger.info("Partially updating prescription with ID: {}", id);

        Ordonnance existingOrdonnance = ordonnanceRepository.findById(id)
                .orElseThrow(() -> new OrdonnanceNotFoundException("Prescription not found with ID: " + id));

        // Only update non-null fields
        if (partialDto.getContenu() != null) {
            validatePrescriptionContent(partialDto.getContenu());
            existingOrdonnance.setContenu(partialDto.getContenu());
        }
        if (partialDto.getDateOrdonnance() != null) {
            existingOrdonnance.setDateOrdonnance(partialDto.getDateOrdonnance());
        }
        if (partialDto.getMedecinUserId() != null) {
            existingOrdonnance.setMedecinUserId(partialDto.getMedecinUserId());
        }
        if (partialDto.getPatientUserId() != null) {
            existingOrdonnance.setPatientUserId(partialDto.getPatientUserId());
        }
        if (partialDto.getDossierMedicalId() != null) {
            validateDossierMedicalExists(partialDto.getDossierMedicalId());
            DossierMedical dossier = new DossierMedical();
            dossier.setId(partialDto.getDossierMedicalId());
            existingOrdonnance.setDossierMedical(dossier);
        }

        Ordonnance updatedOrdonnance = ordonnanceRepository.save(existingOrdonnance);

        logger.info("Partially updated prescription with ID: {}", updatedOrdonnance.getId());
        return ordonnanceMapper.toDto(updatedOrdonnance);
    }

    /**
     * Updates only the prescription content.
     */
    @Transactional
    public OrdonnanceDto updatePrescriptionContent(Long id, String newContent) {
        logger.info("Updating prescription content for ID: {}", id);

        Ordonnance ordonnance = ordonnanceRepository.findById(id)
                .orElseThrow(() -> new OrdonnanceNotFoundException("Prescription not found with ID: " + id));

        validatePrescriptionContent(newContent);
        ordonnance.setContenu(newContent);

        Ordonnance updatedOrdonnance = ordonnanceRepository.save(ordonnance);

        logger.info("Updated prescription content for ID: {}", updatedOrdonnance.getId());
        return ordonnanceMapper.toDto(updatedOrdonnance);
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Deletes a prescription by ID.
     */
    @Transactional
    public void deleteOrdonnance(Long id) {
        logger.info("Deleting prescription with ID: {}", id);

        if (!ordonnanceRepository.existsById(id)) {
            throw new OrdonnanceNotFoundException("Prescription not found with ID: " + id);
        }

        ordonnanceRepository.deleteById(id);
        logger.info("Deleted prescription with ID: {}", id);
    }

    /**
     * Deletes multiple prescriptions by IDs.
     */
    @Transactional
    public void deleteOrdonnancesBatch(List<Long> ids) {
        logger.info("Deleting {} prescriptions in batch", ids.size());

        // Validate all exist
        ids.forEach(id -> {
            if (!ordonnanceRepository.existsById(id)) {
                throw new OrdonnanceNotFoundException("Prescription not found with ID: " + id);
            }
        });

        ordonnanceRepository.deleteAllById(ids);
        logger.info("Deleted {} prescriptions successfully", ids.size());
    }

    /**
     * Deletes all prescriptions for a specific dossier medical.
     */
    @Transactional
    public void deleteByDossierMedical(Long dossierMedicalId) {
        logger.info("Deleting all prescriptions for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        List<Ordonnance> ordonnances = ordonnanceRepository.findByDossierMedicalId(dossierMedicalId);
        ordonnanceRepository.deleteAll(ordonnances);

        logger.info("Deleted {} prescriptions for dossier medical: {}", ordonnances.size(), dossierMedicalId);
    }

    /**
     * Deletes all prescriptions for a specific patient.
     */
    @Transactional
    public void deleteByPatient(Long patientUserId) {
        logger.info("Deleting all prescriptions for patient: {}", patientUserId);

        List<Ordonnance> ordonnances = ordonnanceRepository.findByPatientUserId(patientUserId);
        ordonnanceRepository.deleteAll(ordonnances);

        logger.info("Deleted {} prescriptions for patient: {}", ordonnances.size(), patientUserId);
    }

    /**
     * Deletes all prescriptions by a specific doctor.
     */
    @Transactional
    public void deleteByMedecin(Long medecinUserId) {
        logger.info("Deleting all prescriptions by doctor: {}", medecinUserId);

        List<Ordonnance> ordonnances = ordonnanceRepository.findByMedecinUserId(medecinUserId);
        ordonnanceRepository.deleteAll(ordonnances);

        logger.info("Deleted {} prescriptions by doctor: {}", ordonnances.size(), medecinUserId);
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Validates that a dossier medical exists.
     */
    private void validateDossierMedicalExists(Long dossierMedicalId) {
        if (dossierMedicalId != null && !dossierMedicalRepository.existsById(dossierMedicalId)) {
            throw new DossierMedicalNotFoundException("Dossier medical not found with ID: " + dossierMedicalId);
        }
    }

    /**
     * Validates prescription content for basic medication format.
     */
    private void validatePrescriptionContent(String contenu) {
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new IllegalArgumentException("Prescription content cannot be empty");
        }

        if (contenu.length() > 1500) {
            throw new IllegalArgumentException("Prescription content cannot exceed 1500 characters");
        }

        // Basic validation for medication format (can be enhanced)
        if (!MEDICATION_PATTERN.matcher(contenu).find()) {
            logger.warn("Prescription content may not contain proper medication format: {}", contenu);
            // Don't throw exception, just log warning as it might be a valid prescription
        }
    }

    /**
     * Checks if a prescription exists.
     */
    public boolean existsById(Long id) {
        return ordonnanceRepository.existsById(id);
    }

    /**
     * Gets prescription by ID or returns empty.
     */
    public Optional<OrdonnanceDto> findByIdOptional(Long id) {
        return ordonnanceRepository.findById(id)
                .map(ordonnanceMapper::toDto);
    }

    /**
     * Validates if a doctor can prescribe to a patient (basic validation).
     */
    public boolean canMedecinPrescribeToPatient(Long medecinUserId, Long patientUserId) {
        // Basic implementation - can be enhanced with more complex authorization logic
        return medecinUserId != null && patientUserId != null;
    }

    /**
     * Gets prescription statistics for a doctor.
     */
    public PrescriptionStats getPrescriptionStatsForMedecin(Long medecinUserId) {
        logger.debug("Getting prescription statistics for doctor: {}", medecinUserId);

        long totalCount = countByMedecin(medecinUserId);
        long todayCount = findTodayPrescriptionsByMedecin(medecinUserId).size();

        return new PrescriptionStats(medecinUserId, totalCount, todayCount);
    }

    /**
     * Gets prescription statistics for a patient.
     */
    public PrescriptionStats getPrescriptionStatsForPatient(Long patientUserId) {
        logger.debug("Getting prescription statistics for patient: {}", patientUserId);

        long totalCount = countByPatient(patientUserId);

        return new PrescriptionStats(patientUserId, totalCount, 0);
    }

    /**
     * Checks if prescription content contains specific medication.
     */
    public boolean containsMedication(String contenu, String medicationName) {
        if (contenu == null || medicationName == null) {
            return false;
        }
        return contenu.toLowerCase().contains(medicationName.toLowerCase());
    }

    // ========================================
    // INNER CLASSES
    // ========================================

    /**
     * Statistics class for prescription data.
     */
    public static class PrescriptionStats {
        private final Long userId;
        private final long totalPrescriptions;
        private final long todayPrescriptions;

        public PrescriptionStats(Long userId, long totalPrescriptions, long todayPrescriptions) {
            this.userId = userId;
            this.totalPrescriptions = totalPrescriptions;
            this.todayPrescriptions = todayPrescriptions;
        }

        public Long getUserId() { return userId; }
        public long getTotalPrescriptions() { return totalPrescriptions; }
        public long getTodayPrescriptions() { return todayPrescriptions; }

        @Override
        public String toString() {
            return "PrescriptionStats{" +
                    "userId=" + userId +
                    ", totalPrescriptions=" + totalPrescriptions +
                    ", todayPrescriptions=" + todayPrescriptions +
                    '}';
        }
    }
}
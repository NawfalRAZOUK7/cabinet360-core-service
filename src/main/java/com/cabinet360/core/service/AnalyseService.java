package com.cabinet360.core.service;

import com.cabinet360.core.dto.AnalyseDto;
import com.cabinet360.core.entity.Analyse;
import com.cabinet360.core.entity.DossierMedical;
import com.cabinet360.core.exception.AnalyseNotFoundException;
import com.cabinet360.core.exception.DossierMedicalNotFoundException;
import com.cabinet360.core.mapper.AnalyseMapper;
import com.cabinet360.core.repository.AnalyseRepository;
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

/**
 * Service for managing medical analyses (Analyse).
 * Provides CRUD operations with proper transaction management.
 */
@Service
@Transactional(readOnly = true)
public class AnalyseService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseService.class);

    private final AnalyseRepository analyseRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final AnalyseMapper analyseMapper;

    @Autowired
    public AnalyseService(AnalyseRepository analyseRepository,
                          DossierMedicalRepository dossierMedicalRepository,
                          AnalyseMapper analyseMapper) {
        this.analyseRepository = analyseRepository;
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.analyseMapper = analyseMapper;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Creates a new analyse for a patient's medical record.
     */
    @Transactional
    public AnalyseDto createAnalyse(AnalyseDto analyseDto) {
        logger.info("Creating new analyse for patient: {}", analyseDto.getPatientUserId());

        // Validate dossier medical exists
        validateDossierMedicalExists(analyseDto.getDossierMedicalId());

        // Set creation date if not provided
        if (analyseDto.getDateAnalyse() == null) {
            analyseDto.setDateAnalyse(LocalDateTime.now());
        }

        // Convert to entity and save
        Analyse analyse = analyseMapper.toEntity(analyseDto);
        Analyse savedAnalyse = analyseRepository.save(analyse);

        logger.info("Created analyse with ID: {}", savedAnalyse.getId());
        return analyseMapper.toDto(savedAnalyse);
    }

    /**
     * Creates multiple analyses in batch.
     */
    @Transactional
    public List<AnalyseDto> createAnalysesBatch(List<AnalyseDto> analyseDtos) {
        logger.info("Creating {} analyses in batch", analyseDtos.size());

        // Validate all dossiers exist
        analyseDtos.forEach(dto -> validateDossierMedicalExists(dto.getDossierMedicalId()));

        // Set creation dates
        analyseDtos.forEach(dto -> {
            if (dto.getDateAnalyse() == null) {
                dto.setDateAnalyse(LocalDateTime.now());
            }
        });

        // Convert and save all
        List<Analyse> analyses = analyseDtos.stream()
                .map(analyseMapper::toEntity)
                .toList();

        List<Analyse> savedAnalyses = analyseRepository.saveAll(analyses);

        logger.info("Created {} analyses successfully", savedAnalyses.size());
        return savedAnalyses.stream()
                .map(analyseMapper::toDto)
                .toList();
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Finds an analyse by its ID.
     */
    public AnalyseDto findById(Long id) {
        logger.debug("Finding analyse by ID: {}", id);

        Analyse analyse = analyseRepository.findById(id)
                .orElseThrow(() -> new AnalyseNotFoundException("Analyse not found with ID: " + id));

        return analyseMapper.toDto(analyse);
    }

    /**
     * Gets all analyses with pagination.
     */
    public Page<AnalyseDto> findAll(int page, int size, String sortBy, String sortDirection) {
        logger.debug("Finding all analyses - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return analyseRepository.findAll(pageable)
                .map(analyseMapper::toDto);
    }

    /**
     * Gets all analyses for a specific medical record.
     */
    public List<AnalyseDto> findByDossierMedical(Long dossierMedicalId) {
        logger.debug("Finding analyses for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        return analyseRepository.findByDossierMedicalId(dossierMedicalId)
                .stream()
                .map(analyseMapper::toDto)
                .toList();
    }

    /**
     * Gets all analyses for a specific patient.
     */
    public List<AnalyseDto> findByPatient(Long patientUserId) {
        logger.debug("Finding analyses for patient: {}", patientUserId);

        return analyseRepository.findByPatientUserId(patientUserId)
                .stream()
                .map(analyseMapper::toDto)
                .toList();
    }

    /**
     * Gets analyses by type.
     */
    public List<AnalyseDto> findByType(String typeAnalyse) {
        logger.debug("Finding analyses by type: {}", typeAnalyse);

        return analyseRepository.findByTypeAnalyse(typeAnalyse)
                .stream()
                .map(analyseMapper::toDto)
                .toList();
    }

    /**
     * Searches analyses by type (partial match).
     */
    public List<AnalyseDto> searchByType(String typeAnalyse) {
        logger.debug("Searching analyses by type: {}", typeAnalyse);

        return analyseRepository.findByTypeAnalyseContainingIgnoreCase(typeAnalyse)
                .stream()
                .map(analyseMapper::toDto)
                .toList();
    }

    /**
     * Gets analyses for a patient within a date range.
     */
    public List<AnalyseDto> findByPatientAndDateRange(Long patientUserId,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate) {
        logger.debug("Finding analyses for patient {} between {} and {}",
                patientUserId, startDate, endDate);

        return analyseRepository.findByPatientAndDateRange(patientUserId, startDate, endDate)
                .stream()
                .map(analyseMapper::toDto)
                .toList();
    }

    /**
     * Searches analyses by result content.
     */
    public List<AnalyseDto> searchByResultContent(String keyword) {
        logger.debug("Searching analyses by result keyword: {}", keyword);

        return analyseRepository.findByResultatContainingIgnoreCase(keyword)
                .stream()
                .map(analyseMapper::toDto)
                .toList();
    }

    /**
     * Gets recent analyses for a patient.
     */
    public List<AnalyseDto> findRecentAnalysesByPatient(Long patientUserId, int limit) {
        logger.debug("Finding {} recent analyses for patient: {}", limit, patientUserId);

        Pageable pageable = PageRequest.of(0, limit);
        return analyseRepository.findRecentAnalysesByPatient(patientUserId)
                .stream()
                .limit(limit)
                .map(analyseMapper::toDto)
                .toList();
    }

    /**
     * Gets analysis count for a patient.
     */
    public long countByPatient(Long patientUserId) {
        logger.debug("Counting analyses for patient: {}", patientUserId);
        return analyseRepository.countByPatientUserId(patientUserId);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Updates an existing analyse.
     */
    @Transactional
    public AnalyseDto updateAnalyse(Long id, AnalyseDto analyseDto) {
        logger.info("Updating analyse with ID: {}", id);

        Analyse existingAnalyse = analyseRepository.findById(id)
                .orElseThrow(() -> new AnalyseNotFoundException("Analyse not found with ID: " + id));

        // Validate dossier medical if changed
        if (analyseDto.getDossierMedicalId() != null &&
                !analyseDto.getDossierMedicalId().equals(existingAnalyse.getDossierMedical().getId())) {
            validateDossierMedicalExists(analyseDto.getDossierMedicalId());
        }

        // Update entity using mapper
        analyseMapper.updateEntityFromDto(analyseDto, existingAnalyse);

        Analyse updatedAnalyse = analyseRepository.save(existingAnalyse);

        logger.info("Updated analyse with ID: {}", updatedAnalyse.getId());
        return analyseMapper.toDto(updatedAnalyse);
    }

    /**
     * Partially updates an analyse.
     */
    @Transactional
    public AnalyseDto partialUpdate(Long id, AnalyseDto partialDto) {
        logger.info("Partially updating analyse with ID: {}", id);

        Analyse existingAnalyse = analyseRepository.findById(id)
                .orElseThrow(() -> new AnalyseNotFoundException("Analyse not found with ID: " + id));

        // Only update non-null fields
        if (partialDto.getTypeAnalyse() != null) {
            existingAnalyse.setTypeAnalyse(partialDto.getTypeAnalyse());
        }
        if (partialDto.getResultat() != null) {
            existingAnalyse.setResultat(partialDto.getResultat());
        }
        if (partialDto.getDateAnalyse() != null) {
            existingAnalyse.setDateAnalyse(partialDto.getDateAnalyse());
        }
        if (partialDto.getPatientUserId() != null) {
            existingAnalyse.setPatientUserId(partialDto.getPatientUserId());
        }
        if (partialDto.getDossierMedicalId() != null) {
            validateDossierMedicalExists(partialDto.getDossierMedicalId());
            DossierMedical dossier = new DossierMedical();
            dossier.setId(partialDto.getDossierMedicalId());
            existingAnalyse.setDossierMedical(dossier);
        }

        Analyse updatedAnalyse = analyseRepository.save(existingAnalyse);

        logger.info("Partially updated analyse with ID: {}", updatedAnalyse.getId());
        return analyseMapper.toDto(updatedAnalyse);
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Deletes an analyse by ID.
     */
    @Transactional
    public void deleteAnalyse(Long id) {
        logger.info("Deleting analyse with ID: {}", id);

        if (!analyseRepository.existsById(id)) {
            throw new AnalyseNotFoundException("Analyse not found with ID: " + id);
        }

        analyseRepository.deleteById(id);
        logger.info("Deleted analyse with ID: {}", id);
    }

    /**
     * Deletes multiple analyses by IDs.
     */
    @Transactional
    public void deleteAnalysesBatch(List<Long> ids) {
        logger.info("Deleting {} analyses in batch", ids.size());

        // Validate all exist
        ids.forEach(id -> {
            if (!analyseRepository.existsById(id)) {
                throw new AnalyseNotFoundException("Analyse not found with ID: " + id);
            }
        });

        analyseRepository.deleteAllById(ids);
        logger.info("Deleted {} analyses successfully", ids.size());
    }

    /**
     * Deletes all analyses for a specific dossier medical.
     */
    @Transactional
    public void deleteByDossierMedical(Long dossierMedicalId) {
        logger.info("Deleting all analyses for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        List<Analyse> analyses = analyseRepository.findByDossierMedicalId(dossierMedicalId);
        analyseRepository.deleteAll(analyses);

        logger.info("Deleted {} analyses for dossier medical: {}", analyses.size(), dossierMedicalId);
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
     * Checks if an analyse exists.
     */
    public boolean existsById(Long id) {
        return analyseRepository.existsById(id);
    }

    /**
     * Gets analyse by ID or returns empty.
     */
    public Optional<AnalyseDto> findByIdOptional(Long id) {
        return analyseRepository.findById(id)
                .map(analyseMapper::toDto);
    }
}
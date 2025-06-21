package com.cabinet360.core.service;

import com.cabinet360.core.dto.NoteMedicaleDto;
import com.cabinet360.core.entity.NoteMedicale;
import com.cabinet360.core.entity.DossierMedical;
import com.cabinet360.core.exception.NoteMedicaleNotFoundException;
import com.cabinet360.core.exception.DossierMedicalNotFoundException;
import com.cabinet360.core.mapper.NoteMedicaleMapper;
import com.cabinet360.core.repository.NoteMedicaleRepository;
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
 * Service for managing medical notes (NoteMedicale).
 * Provides CRUD operations with proper transaction management and medical note validation.
 */
@Service
@Transactional(readOnly = true)
public class NoteMedicaleService {

    private static final Logger logger = LoggerFactory.getLogger(NoteMedicaleService.class);

    // Minimum note length for detailed notes
    private static final int MIN_DETAILED_NOTE_LENGTH = 50;

    // Maximum note length
    private static final int MAX_NOTE_LENGTH = 1000;

    private final NoteMedicaleRepository noteMedicaleRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final NoteMedicaleMapper noteMedicaleMapper;

    @Autowired
    public NoteMedicaleService(NoteMedicaleRepository noteMedicaleRepository,
                               DossierMedicalRepository dossierMedicalRepository,
                               NoteMedicaleMapper noteMedicaleMapper) {
        this.noteMedicaleRepository = noteMedicaleRepository;
        this.dossierMedicalRepository = dossierMedicalRepository;
        this.noteMedicaleMapper = noteMedicaleMapper;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Creates a new medical note.
     */
    @Transactional
    public NoteMedicaleDto createNote(NoteMedicaleDto noteMedicaleDto) {
        logger.info("Creating new medical note for dossier: {} by doctor: {}",
                noteMedicaleDto.getDossierMedicalId(), noteMedicaleDto.getMedecinUserId());

        // Validate dossier medical exists
        validateDossierMedicalExists(noteMedicaleDto.getDossierMedicalId());

        // Set note date if not provided
        if (noteMedicaleDto.getDateNote() == null) {
            noteMedicaleDto.setDateNote(LocalDateTime.now());
        }

        // Validate note content
        validateNoteContent(noteMedicaleDto.getContenu());

        // Convert to entity and save
        NoteMedicale noteMedicale = noteMedicaleMapper.toEntity(noteMedicaleDto);
        NoteMedicale savedNote = noteMedicaleRepository.save(noteMedicale);

        logger.info("Created medical note with ID: {}", savedNote.getId());
        return noteMedicaleMapper.toDto(savedNote);
    }

    /**
     * Creates multiple medical notes in batch.
     */
    @Transactional
    public List<NoteMedicaleDto> createNotesBatch(List<NoteMedicaleDto> noteMedicaleDtos) {
        logger.info("Creating {} medical notes in batch", noteMedicaleDtos.size());

        // Validate all dossiers exist
        noteMedicaleDtos.forEach(dto -> validateDossierMedicalExists(dto.getDossierMedicalId()));

        // Set note dates and validate content
        noteMedicaleDtos.forEach(dto -> {
            if (dto.getDateNote() == null) {
                dto.setDateNote(LocalDateTime.now());
            }
            validateNoteContent(dto.getContenu());
        });

        // Convert and save all
        List<NoteMedicale> notes = noteMedicaleDtos.stream()
                .map(noteMedicaleMapper::toEntity)
                .toList();

        List<NoteMedicale> savedNotes = noteMedicaleRepository.saveAll(notes);

        logger.info("Created {} medical notes successfully", savedNotes.size());
        return savedNotes.stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Finds a medical note by its ID.
     */
    public NoteMedicaleDto findById(Long id) {
        logger.debug("Finding medical note by ID: {}", id);

        NoteMedicale noteMedicale = noteMedicaleRepository.findById(id)
                .orElseThrow(() -> new NoteMedicaleNotFoundException("Medical note not found with ID: " + id));

        return noteMedicaleMapper.toDto(noteMedicale);
    }

    /**
     * Gets all medical notes with pagination.
     */
    public Page<NoteMedicaleDto> findAll(int page, int size, String sortBy, String sortDirection) {
        logger.debug("Finding all medical notes - page: {}, size: {}", page, size);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return noteMedicaleRepository.findAll(pageable)
                .map(noteMedicaleMapper::toDto);
    }

    /**
     * Gets all notes for a specific medical record.
     */
    public List<NoteMedicaleDto> findByDossierMedical(Long dossierMedicalId) {
        logger.debug("Finding notes for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        return noteMedicaleRepository.findByDossierMedicalId(dossierMedicalId)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets all notes written by a specific doctor.
     */
    public List<NoteMedicaleDto> findByMedecin(Long medecinUserId) {
        logger.debug("Finding notes by doctor: {}", medecinUserId);

        return noteMedicaleRepository.findByMedecinUserId(medecinUserId)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets notes for a specific patient.
     */
    public List<NoteMedicaleDto> findByPatient(Long patientUserId) {
        logger.debug("Finding notes for patient: {}", patientUserId);

        return noteMedicaleRepository.findByPatientUserId(patientUserId)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets notes within a date range.
     */
    public List<NoteMedicaleDto> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Finding notes between {} and {}", startDate, endDate);

        return noteMedicaleRepository.findByDateNoteRange(startDate, endDate)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets notes by doctor within a date range.
     */
    public List<NoteMedicaleDto> findByMedecinAndDateRange(Long medecinUserId,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate) {
        logger.debug("Finding notes by doctor {} between {} and {}",
                medecinUserId, startDate, endDate);

        return noteMedicaleRepository.findByMedecinAndDateRange(medecinUserId, startDate, endDate)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Searches notes by content keyword.
     */
    public List<NoteMedicaleDto> searchByContent(String keyword) {
        logger.debug("Searching notes by content keyword: {}", keyword);

        return noteMedicaleRepository.findByContenuContainingIgnoreCase(keyword)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Searches detailed notes by keyword (minimum length filter).
     */
    public List<NoteMedicaleDto> searchDetailedNotes(String keyword) {
        logger.debug("Searching detailed notes by keyword: {}", keyword);

        return noteMedicaleRepository.findDetailedNotesByKeyword(keyword, MIN_DETAILED_NOTE_LENGTH)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets notes for a specific doctor-dossier combination.
     */
    public List<NoteMedicaleDto> findByDossierAndMedecin(Long dossierMedicalId, Long medecinUserId) {
        logger.debug("Finding notes for dossier {} by doctor {}", dossierMedicalId, medecinUserId);

        validateDossierMedicalExists(dossierMedicalId);

        return noteMedicaleRepository.findByDossierMedicalIdAndMedecinUserId(dossierMedicalId, medecinUserId)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets recent notes for a dossier medical.
     */
    public List<NoteMedicaleDto> findRecentNotesByDossier(Long dossierMedicalId, int limit) {
        logger.debug("Finding {} recent notes for dossier: {}", limit, dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        return noteMedicaleRepository.findRecentNotesByDossier(dossierMedicalId)
                .stream()
                .limit(limit)
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets recent notes by a doctor.
     */
    public List<NoteMedicaleDto> findRecentNotesByMedecin(Long medecinUserId, int limit) {
        logger.debug("Finding {} recent notes by doctor: {}", limit, medecinUserId);

        return noteMedicaleRepository.findRecentNotesByMedecin(medecinUserId)
                .stream()
                .limit(limit)
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets today's notes by a doctor.
     */
    public List<NoteMedicaleDto> findTodayNotesByMedecin(Long medecinUserId) {
        logger.debug("Finding today's notes by doctor: {}", medecinUserId);

        return noteMedicaleRepository.findTodayNotesByMedecin(medecinUserId)
                .stream()
                .map(noteMedicaleMapper::toDto)
                .toList();
    }

    /**
     * Gets note count by doctor.
     */
    public long countByMedecin(Long medecinUserId) {
        logger.debug("Counting notes by doctor: {}", medecinUserId);
        return noteMedicaleRepository.countByMedecinUserId(medecinUserId);
    }

    /**
     * Gets note count by dossier medical.
     */
    public long countByDossierMedical(Long dossierMedicalId) {
        logger.debug("Counting notes for dossier medical: {}", dossierMedicalId);
        return noteMedicaleRepository.countByDossierMedicalId(dossierMedicalId);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Updates an existing medical note.
     */
    @Transactional
    public NoteMedicaleDto updateNote(Long id, NoteMedicaleDto noteMedicaleDto) {
        logger.info("Updating medical note with ID: {}", id);

        NoteMedicale existingNote = noteMedicaleRepository.findById(id)
                .orElseThrow(() -> new NoteMedicaleNotFoundException("Medical note not found with ID: " + id));

        // Validate dossier medical if changed
        if (noteMedicaleDto.getDossierMedicalId() != null &&
                !noteMedicaleDto.getDossierMedicalId().equals(existingNote.getDossierMedical().getId())) {
            validateDossierMedicalExists(noteMedicaleDto.getDossierMedicalId());
        }

        // Validate note content if changed
        if (noteMedicaleDto.getContenu() != null) {
            validateNoteContent(noteMedicaleDto.getContenu());
        }

        // Update entity using mapper
        noteMedicaleMapper.updateEntityFromDto(noteMedicaleDto, existingNote);

        NoteMedicale updatedNote = noteMedicaleRepository.save(existingNote);

        logger.info("Updated medical note with ID: {}", updatedNote.getId());
        return noteMedicaleMapper.toDto(updatedNote);
    }

    /**
     * Partially updates a medical note.
     */
    @Transactional
    public NoteMedicaleDto partialUpdate(Long id, NoteMedicaleDto partialDto) {
        logger.info("Partially updating medical note with ID: {}", id);

        NoteMedicale existingNote = noteMedicaleRepository.findById(id)
                .orElseThrow(() -> new NoteMedicaleNotFoundException("Medical note not found with ID: " + id));

        // Only update non-null fields
        if (partialDto.getContenu() != null) {
            validateNoteContent(partialDto.getContenu());
            existingNote.setContenu(partialDto.getContenu());
        }
        if (partialDto.getDateNote() != null) {
            existingNote.setDateNote(partialDto.getDateNote());
        }
        if (partialDto.getMedecinUserId() != null) {
            existingNote.setMedecinUserId(partialDto.getMedecinUserId());
        }
        if (partialDto.getDossierMedicalId() != null) {
            validateDossierMedicalExists(partialDto.getDossierMedicalId());
            DossierMedical dossier = new DossierMedical();
            dossier.setId(partialDto.getDossierMedicalId());
            existingNote.setDossierMedical(dossier);
        }

        NoteMedicale updatedNote = noteMedicaleRepository.save(existingNote);

        logger.info("Partially updated medical note with ID: {}", updatedNote.getId());
        return noteMedicaleMapper.toDto(updatedNote);
    }

    /**
     * Updates only the note content.
     */
    @Transactional
    public NoteMedicaleDto updateNoteContent(Long id, String newContent) {
        logger.info("Updating note content for ID: {}", id);

        NoteMedicale note = noteMedicaleRepository.findById(id)
                .orElseThrow(() -> new NoteMedicaleNotFoundException("Medical note not found with ID: " + id));

        validateNoteContent(newContent);
        note.setContenu(newContent);

        NoteMedicale updatedNote = noteMedicaleRepository.save(note);

        logger.info("Updated note content for ID: {}", updatedNote.getId());
        return noteMedicaleMapper.toDto(updatedNote);
    }

    /**
     * Appends content to an existing note.
     */
    @Transactional
    public NoteMedicaleDto appendToNote(Long id, String additionalContent) {
        logger.info("Appending content to note with ID: {}", id);

        NoteMedicale note = noteMedicaleRepository.findById(id)
                .orElseThrow(() -> new NoteMedicaleNotFoundException("Medical note not found with ID: " + id));

        String newContent = note.getContenu() + "\n" + additionalContent;
        validateNoteContent(newContent);
        note.setContenu(newContent);

        NoteMedicale updatedNote = noteMedicaleRepository.save(note);

        logger.info("Appended content to note with ID: {}", updatedNote.getId());
        return noteMedicaleMapper.toDto(updatedNote);
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Deletes a medical note by ID.
     */
    @Transactional
    public void deleteNote(Long id) {
        logger.info("Deleting medical note with ID: {}", id);

        if (!noteMedicaleRepository.existsById(id)) {
            throw new NoteMedicaleNotFoundException("Medical note not found with ID: " + id);
        }

        noteMedicaleRepository.deleteById(id);
        logger.info("Deleted medical note with ID: {}", id);
    }

    /**
     * Deletes multiple notes by IDs.
     */
    @Transactional
    public void deleteNotesBatch(List<Long> ids) {
        logger.info("Deleting {} medical notes in batch", ids.size());

        // Validate all exist
        ids.forEach(id -> {
            if (!noteMedicaleRepository.existsById(id)) {
                throw new NoteMedicaleNotFoundException("Medical note not found with ID: " + id);
            }
        });

        noteMedicaleRepository.deleteAllById(ids);
        logger.info("Deleted {} medical notes successfully", ids.size());
    }

    /**
     * Deletes all notes for a specific dossier medical.
     */
    @Transactional
    public void deleteByDossierMedical(Long dossierMedicalId) {
        logger.info("Deleting all notes for dossier medical: {}", dossierMedicalId);

        validateDossierMedicalExists(dossierMedicalId);

        List<NoteMedicale> notes = noteMedicaleRepository.findByDossierMedicalId(dossierMedicalId);
        noteMedicaleRepository.deleteAll(notes);

        logger.info("Deleted {} notes for dossier medical: {}", notes.size(), dossierMedicalId);
    }

    /**
     * Deletes all notes by a specific doctor.
     */
    @Transactional
    public void deleteByMedecin(Long medecinUserId) {
        logger.info("Deleting all notes by doctor: {}", medecinUserId);

        List<NoteMedicale> notes = noteMedicaleRepository.findByMedecinUserId(medecinUserId);
        noteMedicaleRepository.deleteAll(notes);

        logger.info("Deleted {} notes by doctor: {}", notes.size(), medecinUserId);
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
     * Validates medical note content.
     */
    private void validateNoteContent(String contenu) {
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new IllegalArgumentException("Note content cannot be empty");
        }

        if (contenu.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("Note content cannot exceed " + MAX_NOTE_LENGTH + " characters");
        }

        // Check for minimum content quality (basic validation)
        if (contenu.trim().length() < 10) {
            logger.warn("Note content is very short (less than 10 characters): {}", contenu);
        }
    }

    /**
     * Checks if a medical note exists.
     */
    public boolean existsById(Long id) {
        return noteMedicaleRepository.existsById(id);
    }

    /**
     * Gets note by ID or returns empty.
     */
    public Optional<NoteMedicaleDto> findByIdOptional(Long id) {
        return noteMedicaleRepository.findById(id)
                .map(noteMedicaleMapper::toDto);
    }

    /**
     * Validates if a doctor can write notes for a dossier.
     */
    public boolean canMedecinWriteNote(Long medecinUserId, Long dossierMedicalId) {
        // Basic implementation - can be enhanced with more complex authorization logic
        return medecinUserId != null && dossierMedicalId != null &&
                dossierMedicalRepository.existsById(dossierMedicalId);
    }

    /**
     * Gets note statistics for a doctor.
     */
    public NoteStats getNoteStatsForMedecin(Long medecinUserId) {
        logger.debug("Getting note statistics for doctor: {}", medecinUserId);

        long totalCount = countByMedecin(medecinUserId);
        long todayCount = findTodayNotesByMedecin(medecinUserId).size();

        return new NoteStats(medecinUserId, totalCount, todayCount);
    }

    /**
     * Gets note statistics for a dossier medical.
     */
    public NoteStats getNoteStatsForDossier(Long dossierMedicalId) {
        logger.debug("Getting note statistics for dossier: {}", dossierMedicalId);

        long totalCount = countByDossierMedical(dossierMedicalId);

        return new NoteStats(dossierMedicalId, totalCount, 0);
    }

    /**
     * Checks if note content contains specific keywords.
     */
    public boolean containsKeywords(String contenu, List<String> keywords) {
        if (contenu == null || keywords == null || keywords.isEmpty()) {
            return false;
        }

        String lowerContent = contenu.toLowerCase();
        return keywords.stream()
                .anyMatch(keyword -> lowerContent.contains(keyword.toLowerCase()));
    }

    /**
     * Gets word count for a note.
     */
    public int getWordCount(String contenu) {
        if (contenu == null || contenu.trim().isEmpty()) {
            return 0;
        }
        return contenu.trim().split("\\s+").length;
    }

    /**
     * Categorizes note by length.
     */
    public NoteCategory categorizeNoteByLength(String contenu) {
        int wordCount = getWordCount(contenu);

        if (wordCount < 10) {
            return NoteCategory.BRIEF;
        } else if (wordCount < 50) {
            return NoteCategory.NORMAL;
        } else {
            return NoteCategory.DETAILED;
        }
    }

    /**
     * Gets summary of notes by category for a doctor.
     */
    public NoteSummary getNoteSummaryForMedecin(Long medecinUserId) {
        logger.debug("Getting note summary for doctor: {}", medecinUserId);

        List<NoteMedicaleDto> notes = findByMedecin(medecinUserId);

        long briefCount = notes.stream()
                .mapToLong(note -> categorizeNoteByLength(note.getContenu()) == NoteCategory.BRIEF ? 1 : 0)
                .sum();

        long normalCount = notes.stream()
                .mapToLong(note -> categorizeNoteByLength(note.getContenu()) == NoteCategory.NORMAL ? 1 : 0)
                .sum();

        long detailedCount = notes.stream()
                .mapToLong(note -> categorizeNoteByLength(note.getContenu()) == NoteCategory.DETAILED ? 1 : 0)
                .sum();

        return new NoteSummary(medecinUserId, briefCount, normalCount, detailedCount);
    }

    // ========================================
    // INNER CLASSES
    // ========================================

    /**
     * Statistics class for note data.
     */
    public static class NoteStats {
        private final Long entityId;
        private final long totalNotes;
        private final long todayNotes;

        public NoteStats(Long entityId, long totalNotes, long todayNotes) {
            this.entityId = entityId;
            this.totalNotes = totalNotes;
            this.todayNotes = todayNotes;
        }

        public Long getEntityId() { return entityId; }
        public long getTotalNotes() { return totalNotes; }
        public long getTodayNotes() { return todayNotes; }

        @Override
        public String toString() {
            return "NoteStats{" +
                    "entityId=" + entityId +
                    ", totalNotes=" + totalNotes +
                    ", todayNotes=" + todayNotes +
                    '}';
        }
    }

    /**
     * Enumeration for note categories based on length.
     */
    public enum NoteCategory {
        BRIEF,      // < 10 words
        NORMAL,     // 10-49 words
        DETAILED    // 50+ words
    }

    /**
     * Summary class for note categorization.
     */
    public static class NoteSummary {
        private final Long medecinUserId;
        private final long briefNotes;
        private final long normalNotes;
        private final long detailedNotes;

        public NoteSummary(Long medecinUserId, long briefNotes, long normalNotes, long detailedNotes) {
            this.medecinUserId = medecinUserId;
            this.briefNotes = briefNotes;
            this.normalNotes = normalNotes;
            this.detailedNotes = detailedNotes;
        }

        public Long getMedecinUserId() { return medecinUserId; }
        public long getBriefNotes() { return briefNotes; }
        public long getNormalNotes() { return normalNotes; }
        public long getDetailedNotes() { return detailedNotes; }
        public long getTotalNotes() { return briefNotes + normalNotes + detailedNotes; }

        @Override
        public String toString() {
            return "NoteSummary{" +
                    "medecinUserId=" + medecinUserId +
                    ", briefNotes=" + briefNotes +
                    ", normalNotes=" + normalNotes +
                    ", detailedNotes=" + detailedNotes +
                    ", totalNotes=" + getTotalNotes() +
                    '}';
        }
    }
}
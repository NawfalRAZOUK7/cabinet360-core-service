package com.cabinet360.core.controller;

import com.cabinet360.core.dto.NoteMedicaleDto;
import com.cabinet360.core.service.NoteMedicaleService;
import com.cabinet360.core.service.NoteMedicaleService.NoteStats;
import com.cabinet360.core.service.NoteMedicaleService.NoteSummary;
import com.cabinet360.core.service.NoteMedicaleService.NoteCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Medical Notes Management
 * Handles all note-related operations including CRUD, search, and analytics
 */
@RestController
@RequestMapping("/api/v1/notes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NoteMedicaleController {

    private static final Logger logger = LoggerFactory.getLogger(NoteMedicaleController.class);

    private final NoteMedicaleService noteMedicaleService;

    public NoteMedicaleController(NoteMedicaleService noteMedicaleService) {
        this.noteMedicaleService = noteMedicaleService;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Create a new medical note
     * POST /api/v1/notes
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<NoteMedicaleDto> createNote(@Valid @RequestBody NoteMedicaleDto noteMedicaleDto) {
        logger.info("🩺 Creating new medical note for dossier: {}", noteMedicaleDto.getDossierMedicalId());

        try {
            NoteMedicaleDto created = noteMedicaleService.createNote(noteMedicaleDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Failed to create medical note: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create multiple medical notes in batch
     * POST /api/v1/notes/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<NoteMedicaleDto>> createNotesBatch(@Valid @RequestBody List<NoteMedicaleDto> notes) {
        logger.info("🩺 Creating {} medical notes in batch", notes.size());

        try {
            List<NoteMedicaleDto> created = noteMedicaleService.createNotesBatch(notes);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Failed to create notes in batch: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Get medical note by ID
     * GET /api/v1/notes/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<NoteMedicaleDto> getNoteById(@PathVariable Long id) {
        logger.info("🔍 Fetching medical note: {}", id);

        try {
            NoteMedicaleDto note = noteMedicaleService.findById(id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            logger.error("❌ Note not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all medical notes with pagination
     * GET /api/v1/notes?page=0&size=10&sortBy=dateNote&sortDirection=DESC
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<NoteMedicaleDto>> getAllNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateNote") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.info("🔍 Fetching all notes - page: {}, size: {}", page, size);

        Page<NoteMedicaleDto> notes = noteMedicaleService.findAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get notes by medical record (dossier medical)
     * GET /api/v1/notes/dossier/{dossierMedicalId}
     */
    @GetMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<NoteMedicaleDto>> getNotesByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🔍 Fetching notes for dossier: {}", dossierMedicalId);

        try {
            List<NoteMedicaleDto> notes = noteMedicaleService.findByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch notes for dossier: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get notes by doctor
     * GET /api/v1/notes/doctor/{medecinUserId}
     */
    @GetMapping("/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteMedicaleDto>> getNotesByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching notes by doctor: {}", medecinUserId);

        List<NoteMedicaleDto> notes = noteMedicaleService.findByMedecin(medecinUserId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get notes by patient
     * GET /api/v1/notes/patient/{patientUserId}
     */
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<NoteMedicaleDto>> getNotesByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching notes for patient: {}", patientUserId);

        List<NoteMedicaleDto> notes = noteMedicaleService.findByPatient(patientUserId);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get notes by doctor and dossier combination
     * GET /api/v1/notes/dossier/{dossierMedicalId}/doctor/{medecinUserId}
     */
    @GetMapping("/dossier/{dossierMedicalId}/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<NoteMedicaleDto>> getNotesByDossierAndDoctor(
            @PathVariable Long dossierMedicalId,
            @PathVariable Long medecinUserId) {

        logger.info("🔍 Fetching notes for dossier {} by doctor {}", dossierMedicalId, medecinUserId);

        try {
            List<NoteMedicaleDto> notes = noteMedicaleService.findByDossierAndMedecin(dossierMedicalId, medecinUserId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch notes: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get notes within date range
     * GET /api/v1/notes/date-range?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteMedicaleDto>> getNotesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("🔍 Fetching notes between {} and {}", startDate, endDate);

        List<NoteMedicaleDto> notes = noteMedicaleService.findByDateRange(startDate, endDate);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get notes by doctor within date range
     * GET /api/v1/notes/doctor/{medecinUserId}/date-range?startDate=...&endDate=...
     */
    @GetMapping("/doctor/{medecinUserId}/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteMedicaleDto>> getNotesByDoctorAndDateRange(
            @PathVariable Long medecinUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("🔍 Fetching notes by doctor {} between {} and {}", medecinUserId, startDate, endDate);

        List<NoteMedicaleDto> notes = noteMedicaleService.findByMedecinAndDateRange(medecinUserId, startDate, endDate);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get recent notes for a dossier
     * GET /api/v1/notes/dossier/{dossierMedicalId}/recent?limit=5
     */
    @GetMapping("/dossier/{dossierMedicalId}/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<NoteMedicaleDto>> getRecentNotesByDossier(
            @PathVariable Long dossierMedicalId,
            @RequestParam(defaultValue = "5") @Min(1) int limit) {

        logger.info("🔍 Fetching {} recent notes for dossier: {}", limit, dossierMedicalId);

        try {
            List<NoteMedicaleDto> notes = noteMedicaleService.findRecentNotesByDossier(dossierMedicalId, limit);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch recent notes: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get recent notes by doctor
     * GET /api/v1/notes/doctor/{medecinUserId}/recent?limit=10
     */
    @GetMapping("/doctor/{medecinUserId}/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteMedicaleDto>> getRecentNotesByDoctor(
            @PathVariable Long medecinUserId,
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        logger.info("🔍 Fetching {} recent notes by doctor: {}", limit, medecinUserId);

        List<NoteMedicaleDto> notes = noteMedicaleService.findRecentNotesByMedecin(medecinUserId, limit);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get today's notes by doctor
     * GET /api/v1/notes/doctor/{medecinUserId}/today
     */
    @GetMapping("/doctor/{medecinUserId}/today")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<NoteMedicaleDto>> getTodayNotesByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching today's notes by doctor: {}", medecinUserId);

        List<NoteMedicaleDto> notes = noteMedicaleService.findTodayNotesByMedecin(medecinUserId);
        return ResponseEntity.ok(notes);
    }

    // ========================================
    // SEARCH OPERATIONS
    // ========================================

    /**
     * Search notes by content keyword
     * GET /api/v1/notes/search?keyword=symptom
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<NoteMedicaleDto>> searchNotesByContent(@RequestParam @NotBlank String keyword) {
        logger.info("🔍 Searching notes by keyword: {}", keyword);

        List<NoteMedicaleDto> notes = noteMedicaleService.searchByContent(keyword);
        return ResponseEntity.ok(notes);
    }

    /**
     * Search detailed notes by keyword (minimum length filter)
     * GET /api/v1/notes/search/detailed?keyword=symptom
     */
    @GetMapping("/search/detailed")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<NoteMedicaleDto>> searchDetailedNotes(@RequestParam @NotBlank String keyword) {
        logger.info("🔍 Searching detailed notes by keyword: {}", keyword);

        List<NoteMedicaleDto> notes = noteMedicaleService.searchDetailedNotes(keyword);
        return ResponseEntity.ok(notes);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Update medical note
     * PUT /api/v1/notes/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<NoteMedicaleDto> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteMedicaleDto noteMedicaleDto) {

        logger.info("🔄 Updating medical note: {}", id);

        try {
            NoteMedicaleDto updated = noteMedicaleService.updateNote(id, noteMedicaleDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to update note: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Partially update medical note
     * PATCH /api/v1/notes/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<NoteMedicaleDto> partialUpdateNote(
            @PathVariable Long id,
            @RequestBody NoteMedicaleDto partialDto) {

        logger.info("🔄 Partially updating medical note: {}", id);

        try {
            NoteMedicaleDto updated = noteMedicaleService.partialUpdate(id, partialDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to partially update note: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update only note content
     * PATCH /api/v1/notes/{id}/content
     */
    @PatchMapping("/{id}/content")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<NoteMedicaleDto> updateNoteContent(
            @PathVariable Long id,
            @RequestBody @NotBlank @Size(max = 1000) String content) {

        logger.info("🔄 Updating note content for ID: {}", id);

        try {
            NoteMedicaleDto updated = noteMedicaleService.updateNoteContent(id, content);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to update note content: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Append content to existing note
     * PATCH /api/v1/notes/{id}/append
     */
    @PatchMapping("/{id}/append")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<NoteMedicaleDto> appendToNote(
            @PathVariable Long id,
            @RequestBody @NotBlank String additionalContent) {

        logger.info("🔄 Appending content to note: {}", id);

        try {
            NoteMedicaleDto updated = noteMedicaleService.appendToNote(id, additionalContent);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to append to note: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Delete medical note
     * DELETE /api/v1/notes/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteNote(@PathVariable Long id) {
        logger.info("🗑️ Deleting medical note: {}", id);

        try {
            noteMedicaleService.deleteNote(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Medical note deleted successfully",
                    "noteId", id.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete note: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete multiple notes in batch
     * DELETE /api/v1/notes/batch
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteNotesBatch(@RequestBody List<Long> ids) {
        logger.info("🗑️ Deleting {} medical notes in batch", ids.size());

        try {
            noteMedicaleService.deleteNotesBatch(ids);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Medical notes deleted successfully",
                    "deletedCount", ids.size(),
                    "noteIds", ids
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete notes in batch: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete all notes for a dossier medical
     * DELETE /api/v1/notes/dossier/{dossierMedicalId}
     */
    @DeleteMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteNotesByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🗑️ Deleting all notes for dossier: {}", dossierMedicalId);

        try {
            noteMedicaleService.deleteByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All notes deleted for dossier medical",
                    "dossierMedicalId", dossierMedicalId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete notes for dossier: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete all notes by doctor
     * DELETE /api/v1/notes/doctor/{medecinUserId}
     */
    @DeleteMapping("/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteNotesByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🗑️ Deleting all notes by doctor: {}", medecinUserId);

        try {
            noteMedicaleService.deleteByMedecin(medecinUserId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All notes deleted for doctor",
                    "medecinUserId", medecinUserId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete notes by doctor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // ========================================
    // ANALYTICS & STATISTICS
    // ========================================

    /**
     * Get note statistics for a doctor
     * GET /api/v1/notes/doctor/{medecinUserId}/stats
     */
    @GetMapping("/doctor/{medecinUserId}/stats")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<NoteStats> getNoteStatsForDoctor(@PathVariable Long medecinUserId) {
        logger.info("📊 Getting note statistics for doctor: {}", medecinUserId);

        NoteStats stats = noteMedicaleService.getNoteStatsForMedecin(medecinUserId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get note statistics for a dossier medical
     * GET /api/v1/notes/dossier/{dossierMedicalId}/stats
     */
    @GetMapping("/dossier/{dossierMedicalId}/stats")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<NoteStats> getNoteStatsForDossier(@PathVariable Long dossierMedicalId) {
        logger.info("📊 Getting note statistics for dossier: {}", dossierMedicalId);

        try {
            NoteStats stats = noteMedicaleService.getNoteStatsForDossier(dossierMedicalId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Failed to get dossier stats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get note summary by categories for a doctor
     * GET /api/v1/notes/doctor/{medecinUserId}/summary
     */
    @GetMapping("/doctor/{medecinUserId}/summary")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<NoteSummary> getNoteSummaryForDoctor(@PathVariable Long medecinUserId) {
        logger.info("📊 Getting note summary for doctor: {}", medecinUserId);

        NoteSummary summary = noteMedicaleService.getNoteSummaryForMedecin(medecinUserId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get note count by doctor
     * GET /api/v1/notes/doctor/{medecinUserId}/count
     */
    @GetMapping("/doctor/{medecinUserId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getNoteCountByDoctor(@PathVariable Long medecinUserId) {
        logger.info("📊 Counting notes by doctor: {}", medecinUserId);

        long count = noteMedicaleService.countByMedecin(medecinUserId);
        return ResponseEntity.ok(Map.of("noteCount", count));
    }

    /**
     * Get note count by dossier medical
     * GET /api/v1/notes/dossier/{dossierMedicalId}/count
     */
    @GetMapping("/dossier/{dossierMedicalId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Long>> getNoteCountByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("📊 Counting notes for dossier: {}", dossierMedicalId);

        long count = noteMedicaleService.countByDossierMedical(dossierMedicalId);
        return ResponseEntity.ok(Map.of("noteCount", count));
    }

    // ========================================
    // UTILITY OPERATIONS
    // ========================================

    /**
     * Check if note exists
     * GET /api/v1/notes/{id}/exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Boolean>> checkNoteExists(@PathVariable Long id) {
        logger.info("🔍 Checking if note exists: {}", id);

        boolean exists = noteMedicaleService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check if doctor can write note for dossier
     * GET /api/v1/notes/can-write?medecinUserId=1&dossierMedicalId=1
     */
    @GetMapping("/can-write")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Boolean>> canDoctorWriteNote(
            @RequestParam Long medecinUserId,
            @RequestParam Long dossierMedicalId) {

        logger.info("🔍 Checking if doctor {} can write note for dossier {}", medecinUserId, dossierMedicalId);

        boolean canWrite = noteMedicaleService.canMedecinWriteNote(medecinUserId, dossierMedicalId);
        return ResponseEntity.ok(Map.of("canWrite", canWrite));
    }

    /**
     * Get word count for note content
     * POST /api/v1/notes/word-count
     */
    @PostMapping("/word-count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> getWordCount(@RequestBody @NotBlank String content) {
        logger.debug("📊 Calculating word count for content");

        int wordCount = noteMedicaleService.getWordCount(content);
        NoteCategory category = noteMedicaleService.categorizeNoteByLength(content);

        return ResponseEntity.ok(Map.of(
                "wordCount", wordCount,
                "category", category,
                "characterCount", content.length()
        ));
    }

    /**
     * Health check endpoint for note service
     * GET /api/v1/notes/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "NoteMedicaleService",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
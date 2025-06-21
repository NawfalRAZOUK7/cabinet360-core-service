package com.cabinet360.core.controller;

import com.cabinet360.core.dto.AnalyseDto;
import com.cabinet360.core.service.AnalyseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
 * REST Controller for Medical Analyses Management
 * Handles all analysis-related operations including CRUD, search, and analytics
 */
@RestController
@RequestMapping("/api/v1/analyses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyseController {

    private static final Logger logger = LoggerFactory.getLogger(AnalyseController.class);

    private final AnalyseService analyseService;

    public AnalyseController(AnalyseService analyseService) {
        this.analyseService = analyseService;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Create a new medical analysis
     * POST /api/v1/analyses
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<AnalyseDto> createAnalysis(@Valid @RequestBody AnalyseDto analyseDto) {
        logger.info("🧪 Creating new analysis for patient: {}", analyseDto.getPatientUserId());

        try {
            AnalyseDto created = analyseService.createAnalyse(analyseDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Failed to create analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create multiple analyses in batch
     * POST /api/v1/analyses/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<AnalyseDto>> createAnalysesBatch(@Valid @RequestBody List<AnalyseDto> analyses) {
        logger.info("🧪 Creating {} analyses in batch", analyses.size());

        try {
            List<AnalyseDto> created = analyseService.createAnalysesBatch(analyses);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Failed to create analyses in batch: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Get analysis by ID
     * GET /api/v1/analyses/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<AnalyseDto> getAnalysisById(@PathVariable Long id) {
        logger.info("🔍 Fetching analysis: {}", id);

        try {
            AnalyseDto analysis = analyseService.findById(id);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            logger.error("❌ Analysis not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all analyses with pagination
     * GET /api/v1/analyses?page=0&size=10&sortBy=dateAnalyse&sortDirection=DESC
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<AnalyseDto>> getAllAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateAnalyse") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.info("🔍 Fetching all analyses - page: {}, size: {}", page, size);

        Page<AnalyseDto> analyses = analyseService.findAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(analyses);
    }

    /**
     * Get analyses by medical record (dossier medical)
     * GET /api/v1/analyses/dossier/{dossierMedicalId}
     */
    @GetMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<AnalyseDto>> getAnalysesByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🔍 Fetching analyses for dossier: {}", dossierMedicalId);

        try {
            List<AnalyseDto> analyses = analyseService.findByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(analyses);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch analyses for dossier: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get analyses by patient
     * GET /api/v1/analyses/patient/{patientUserId}
     */
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<List<AnalyseDto>> getAnalysesByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching analyses for patient: {}", patientUserId);

        List<AnalyseDto> analyses = analyseService.findByPatient(patientUserId);
        return ResponseEntity.ok(analyses);
    }

    /**
     * Get analyses by type
     * GET /api/v1/analyses/type/{typeAnalyse}
     */
    @GetMapping("/type/{typeAnalyse}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<AnalyseDto>> getAnalysesByType(@PathVariable String typeAnalyse) {
        logger.info("🔍 Fetching analyses by type: {}", typeAnalyse);

        List<AnalyseDto> analyses = analyseService.findByType(typeAnalyse);
        return ResponseEntity.ok(analyses);
    }

    /**
     * Get analyses by patient within date range
     * GET /api/v1/analyses/patient/{patientUserId}/date-range?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/patient/{patientUserId}/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<List<AnalyseDto>> getAnalysesByPatientAndDateRange(
            @PathVariable Long patientUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("🔍 Fetching analyses for patient {} between {} and {}", patientUserId, startDate, endDate);

        List<AnalyseDto> analyses = analyseService.findByPatientAndDateRange(patientUserId, startDate, endDate);
        return ResponseEntity.ok(analyses);
    }

    /**
     * Get recent analyses for a patient
     * GET /api/v1/analyses/patient/{patientUserId}/recent?limit=5
     */
    @GetMapping("/patient/{patientUserId}/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<List<AnalyseDto>> getRecentAnalysesByPatient(
            @PathVariable Long patientUserId,
            @RequestParam(defaultValue = "5") @Min(1) int limit) {

        logger.info("🔍 Fetching {} recent analyses for patient: {}", limit, patientUserId);

        List<AnalyseDto> analyses = analyseService.findRecentAnalysesByPatient(patientUserId, limit);
        return ResponseEntity.ok(analyses);
    }

    // ========================================
    // SEARCH OPERATIONS
    // ========================================

    /**
     * Search analyses by type (partial match)
     * GET /api/v1/analyses/search/type?typeAnalyse=blood
     */
    @GetMapping("/search/type")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<AnalyseDto>> searchAnalysesByType(@RequestParam @NotBlank String typeAnalyse) {
        logger.info("🔍 Searching analyses by type: {}", typeAnalyse);

        List<AnalyseDto> analyses = analyseService.searchByType(typeAnalyse);
        return ResponseEntity.ok(analyses);
    }

    /**
     * Search analyses by result content
     * GET /api/v1/analyses/search/result?keyword=normal
     */
    @GetMapping("/search/result")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<AnalyseDto>> searchAnalysesByResult(@RequestParam @NotBlank String keyword) {
        logger.info("🔍 Searching analyses by result keyword: {}", keyword);

        List<AnalyseDto> analyses = analyseService.searchByResultContent(keyword);
        return ResponseEntity.ok(analyses);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Update analysis
     * PUT /api/v1/analyses/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<AnalyseDto> updateAnalysis(
            @PathVariable Long id,
            @Valid @RequestBody AnalyseDto analyseDto) {

        logger.info("🔄 Updating analysis: {}", id);

        try {
            AnalyseDto updated = analyseService.updateAnalyse(id, analyseDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to update analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Partially update analysis
     * PATCH /api/v1/analyses/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<AnalyseDto> partialUpdateAnalysis(
            @PathVariable Long id,
            @RequestBody AnalyseDto partialDto) {

        logger.info("🔄 Partially updating analysis: {}", id);

        try {
            AnalyseDto updated = analyseService.partialUpdate(id, partialDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to partially update analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Delete analysis
     * DELETE /api/v1/analyses/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAnalysis(@PathVariable Long id) {
        logger.info("🗑️ Deleting analysis: {}", id);

        try {
            analyseService.deleteAnalyse(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Analysis deleted successfully",
                    "analysisId", id.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete analysis: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete multiple analyses in batch
     * DELETE /api/v1/analyses/batch
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAnalysesBatch(@RequestBody List<Long> ids) {
        logger.info("🗑️ Deleting {} analyses in batch", ids.size());

        try {
            analyseService.deleteAnalysesBatch(ids);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Analyses deleted successfully",
                    "deletedCount", ids.size(),
                    "analysisIds", ids
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete analyses in batch: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete all analyses for a dossier medical
     * DELETE /api/v1/analyses/dossier/{dossierMedicalId}
     */
    @DeleteMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAnalysesByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🗑️ Deleting all analyses for dossier: {}", dossierMedicalId);

        try {
            analyseService.deleteByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All analyses deleted for dossier medical",
                    "dossierMedicalId", dossierMedicalId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete analyses for dossier: {}", e.getMessage());
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
     * Get analysis count by patient
     * GET /api/v1/analyses/patient/{patientUserId}/count
     */
    @GetMapping("/patient/{patientUserId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<Map<String, Long>> getAnalysisCountByPatient(@PathVariable Long patientUserId) {
        logger.info("📊 Counting analyses for patient: {}", patientUserId);

        long count = analyseService.countByPatient(patientUserId);
        return ResponseEntity.ok(Map.of("analysisCount", count));
    }

    /**
     * Get analysis statistics by type
     * GET /api/v1/analyses/stats/types
     */
    @GetMapping("/stats/types")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAnalysisStatsByType() {
        logger.info("📊 Getting analysis statistics by type");

        // This would typically involve aggregation queries
        // For now, returning a placeholder structure
        return ResponseEntity.ok(Map.of(
                "message", "Analysis statistics by type",
                "note", "This endpoint would contain aggregated data by analysis type"
        ));
    }

    /**
     * Get analysis timeline for a patient
     * GET /api/v1/analyses/patient/{patientUserId}/timeline
     */
    @GetMapping("/patient/{patientUserId}/timeline")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<List<AnalyseDto>> getAnalysisTimelineForPatient(
            @PathVariable Long patientUserId,
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        logger.info("📊 Getting analysis timeline for patient: {} (limit: {})", patientUserId, limit);

        List<AnalyseDto> timeline = analyseService.findRecentAnalysesByPatient(patientUserId, limit);
        return ResponseEntity.ok(timeline);
    }

    // ========================================
    // UTILITY OPERATIONS
    // ========================================

    /**
     * Check if analysis exists
     * GET /api/v1/analyses/{id}/exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Boolean>> checkAnalysisExists(@PathVariable Long id) {
        logger.info("🔍 Checking if analysis exists: {}", id);

        boolean exists = analyseService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Get analysis types summary
     * GET /api/v1/analyses/types/summary
     */
    @GetMapping("/types/summary")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> getAnalysisTypesSummary() {
        logger.info("📊 Getting analysis types summary");

        // This would typically involve distinct queries on typeAnalyse
        return ResponseEntity.ok(Map.of(
                "message", "Analysis types summary",
                "commonTypes", List.of(
                        "Blood Test",
                        "X-Ray",
                        "MRI",
                        "CT Scan",
                        "Ultrasound",
                        "ECG",
                        "Urine Test"
                ),
                "note", "This endpoint would contain actual type statistics from the database"
        ));
    }

    /**
     * Health check endpoint for analysis service
     * GET /api/v1/analyses/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AnalyseService",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
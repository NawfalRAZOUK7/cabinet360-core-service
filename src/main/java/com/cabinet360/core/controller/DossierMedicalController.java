package com.cabinet360.core.controller;

import com.cabinet360.core.dto.DossierMedicalDto;
import com.cabinet360.core.service.DossierMedicalService;
import com.cabinet360.core.service.DossierMedicalService.CompletePatientRecord;
import com.cabinet360.core.service.DossierMedicalService.DossierStats;
import jakarta.validation.Valid;
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
 * REST Controller for Medical Records (DossierMedical) Management
 * Handles all medical record operations with proper access control
 */
@RestController
@RequestMapping("/api/v1/dossiers-medicaux")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DossierMedicalController {

    private static final Logger logger = LoggerFactory.getLogger(DossierMedicalController.class);

    private final DossierMedicalService dossierMedicalService;

    public DossierMedicalController(DossierMedicalService dossierMedicalService) {
        this.dossierMedicalService = dossierMedicalService;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Create a new medical record
     * POST /api/v1/dossiers-medicaux
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DossierMedicalDto> createDossierMedical(@Valid @RequestBody DossierMedicalDto dossierMedicalDto) {
        logger.info("🏥 Creating medical record for patient: {}", dossierMedicalDto.getPatientUserId());

        try {
            DossierMedicalDto created = dossierMedicalService.createDossierMedical(dossierMedicalDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Medical record creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Get medical record by ID
     * GET /api/v1/dossiers-medicaux/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<DossierMedicalDto> getDossierMedicalById(@PathVariable Long id) {
        logger.info("🔍 Fetching medical record: {}", id);

        try {
            DossierMedicalDto dossier = dossierMedicalService.findById(id);
            return ResponseEntity.ok(dossier);
        } catch (Exception e) {
            logger.error("❌ Medical record not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get medical record by patient ID
     * GET /api/v1/dossiers-medicaux/patient/{patientUserId}
     */
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<DossierMedicalDto> getDossierMedicalByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching medical record for patient: {}", patientUserId);

        try {
            DossierMedicalDto dossier = dossierMedicalService.findByPatientUserId(patientUserId);
            return ResponseEntity.ok(dossier);
        } catch (Exception e) {
            logger.error("❌ Medical record not found for patient: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get complete patient record with all related data
     * GET /api/v1/dossiers-medicaux/patient/{patientUserId}/complete
     */
    @GetMapping("/patient/{patientUserId}/complete")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<CompletePatientRecord> getCompletePatientRecord(@PathVariable Long patientUserId) {
        logger.info("📋 Fetching complete medical record for patient: {}", patientUserId);

        try {
            CompletePatientRecord complete = dossierMedicalService.getCompletePatientRecord(patientUserId);
            return ResponseEntity.ok(complete);
        } catch (Exception e) {
            logger.error("❌ Complete record not found for patient: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all medical records with pagination
     * GET /api/v1/dossiers-medicaux?page=0&size=20&sortBy=createdAt&sortDirection=DESC
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<Page<DossierMedicalDto>> getAllDossiersMedicaux(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        logger.info("📋 Fetching medical records - page: {}, size: {}", page, size);

        Page<DossierMedicalDto> dossiers = dossierMedicalService.findAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Get medical records by doctor
     * GET /api/v1/dossiers-medicaux/doctor/{medecinUserId}
     */
    @GetMapping("/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DossierMedicalDto>> getDossiersByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching medical records for doctor: {}", medecinUserId);

        List<DossierMedicalDto> dossiers = dossierMedicalService.findByMedecin(medecinUserId);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Get medical records accessible by a doctor (including shared ones)
     * GET /api/v1/dossiers-medicaux/doctor/{doctorUserId}/accessible
     */
    @GetMapping("/doctor/{doctorUserId}/accessible")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DossierMedicalDto>> getAccessibleDossiersByDoctor(@PathVariable Long doctorUserId) {
        logger.info("🔍 Fetching accessible medical records for doctor: {}", doctorUserId);

        List<DossierMedicalDto> dossiers = dossierMedicalService.findAccessibleByDoctor(doctorUserId);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Get medical records by status
     * GET /api/v1/dossiers-medicaux/status/{statut}
     */
    @GetMapping("/status/{statut}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<DossierMedicalDto>> getDossiersByStatus(@PathVariable String statut) {
        logger.info("🔍 Fetching medical records with status: {}", statut);

        List<DossierMedicalDto> dossiers = dossierMedicalService.findByStatus(statut);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Search medical records
     * GET /api/v1/dossiers-medicaux/search?patientId=1&medecinId=2&keyword=diabetes
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DossierMedicalDto>> searchDossiers(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long medecinId,
            @RequestParam(required = false) String keyword) {
        logger.info("🔍 Searching medical records - patient: {}, medecin: {}, keyword: {}",
                patientId, medecinId, keyword);

        List<DossierMedicalDto> dossiers = dossierMedicalService.searchDossiers(patientId, medecinId, keyword);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Get medical records by date range
     * GET /api/v1/dossiers-medicaux/date-range?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<DossierMedicalDto>> getDossiersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        logger.info("🔍 Fetching medical records between {} and {}", start, end);

        List<DossierMedicalDto> dossiers = dossierMedicalService.findByDateRange(start, end);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Get recently accessed medical records
     * GET /api/v1/dossiers-medicaux/recent?limit=10
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DossierMedicalDto>> getRecentlyAccessedDossiers(
            @RequestParam(defaultValue = "10") int limit) {
        logger.info("🔍 Fetching {} recently accessed medical records", limit);

        List<DossierMedicalDto> dossiers = dossierMedicalService.findRecentlyAccessed(limit);
        return ResponseEntity.ok(dossiers);
    }

    /**
     * Get medical records needing attention
     * GET /api/v1/dossiers-medicaux/needs-attention?days=30
     */
    @GetMapping("/needs-attention")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<DossierMedicalDto>> getDossiersNeedingAttention(
            @RequestParam(defaultValue = "30") int days) {
        logger.info("🔍 Fetching medical records not accessed for {} days", days);

        List<DossierMedicalDto> dossiers = dossierMedicalService.findDossiersNeedingAttention(days);
        return ResponseEntity.ok(dossiers);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Update medical record
     * PUT /api/v1/dossiers-medicaux/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DossierMedicalDto> updateDossierMedical(
            @PathVariable Long id,
            @Valid @RequestBody DossierMedicalDto dossierMedicalDto) {
        logger.info("🔄 Updating medical record: {}", id);

        try {
            DossierMedicalDto updated = dossierMedicalService.updateDossierMedical(id, dossierMedicalDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Medical record update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Partial update medical record
     * PATCH /api/v1/dossiers-medicaux/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DossierMedicalDto> partialUpdateDossierMedical(
            @PathVariable Long id,
            @RequestBody DossierMedicalDto partialDto) {
        logger.info("🔄 Partially updating medical record: {}", id);

        try {
            DossierMedicalDto updated = dossierMedicalService.partialUpdate(id, partialDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Partial update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update medical record status
     * PATCH /api/v1/dossiers-medicaux/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DossierMedicalDto> updateDossierStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        logger.info("🔄 Updating status for medical record {}: {}", id, status);

        try {
            DossierMedicalDto updated = dossierMedicalService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Status update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Assign doctor to medical record
     * PATCH /api/v1/dossiers-medicaux/{id}/assign-doctor
     */
    @PatchMapping("/{id}/assign-doctor")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<DossierMedicalDto> assignDoctor(
            @PathVariable Long id,
            @RequestParam Long medecinUserId) {
        logger.info("🔄 Assigning doctor {} to medical record {}", medecinUserId, id);

        try {
            DossierMedicalDto updated = dossierMedicalService.assignMedecin(id, medecinUserId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Doctor assignment failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add authorized doctor to medical record
     * PATCH /api/v1/dossiers-medicaux/{id}/add-authorized-doctor
     */
    @PatchMapping("/{id}/add-authorized-doctor")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DossierMedicalDto> addAuthorizedDoctor(
            @PathVariable Long id,
            @RequestParam Long doctorUserId) {
        logger.info("🔄 Adding authorized doctor {} to medical record {}", doctorUserId, id);

        try {
            DossierMedicalDto updated = dossierMedicalService.addAuthorizedMedecin(id, doctorUserId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Adding authorized doctor failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update historical summary
     * PATCH /api/v1/dossiers-medicaux/{id}/historical-summary
     */
    @PatchMapping("/{id}/historical-summary")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DossierMedicalDto> updateHistoricalSummary(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        logger.info("🔄 Updating historical summary for medical record: {}", id);

        try {
            String historicalSummary = request.get("historicalSummary");
            DossierMedicalDto updated = dossierMedicalService.updateHistoricalSummary(id, historicalSummary);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Historical summary update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Archive medical record (soft delete)
     * PATCH /api/v1/dossiers-medicaux/{id}/archive
     */
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<DossierMedicalDto> archiveDossierMedical(@PathVariable Long id) {
        logger.info("📦 Archiving medical record: {}", id);

        try {
            DossierMedicalDto archived = dossierMedicalService.archiveDossierMedical(id);
            return ResponseEntity.ok(archived);
        } catch (Exception e) {
            logger.error("❌ Medical record archiving failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Permanently delete medical record (⚠️ DANGEROUS)
     * DELETE /api/v1/dossiers-medicaux/{id}/permanent
     */
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDossierMedicalPermanently(@PathVariable Long id) {
        logger.warn("⚠️ PERMANENTLY deleting medical record: {}", id);

        try {
            dossierMedicalService.deleteDossierMedicalPermanently(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Medical record permanently deleted",
                    "dossierId", id.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Permanent deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // UTILITY ENDPOINTS
    // ========================================

    /**
     * Check if medical record exists
     * GET /api/v1/dossiers-medicaux/{id}/exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, Boolean>> checkDossierExists(@PathVariable Long id) {
        logger.info("🔍 Checking if medical record exists: {}", id);

        boolean exists = dossierMedicalService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check if patient has medical record
     * GET /api/v1/dossiers-medicaux/patient/{patientUserId}/exists
     */
    @GetMapping("/patient/{patientUserId}/exists")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, Boolean>> checkPatientHasDossier(@PathVariable Long patientUserId) {
        logger.info("🔍 Checking if patient has medical record: {}", patientUserId);

        boolean exists = dossierMedicalService.existsByPatientUserId(patientUserId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check doctor access permissions
     * GET /api/v1/dossiers-medicaux/{id}/access/{doctorUserId}
     */
    @GetMapping("/{id}/access/{doctorUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkDoctorAccess(
            @PathVariable Long id,
            @PathVariable Long doctorUserId) {
        logger.info("🔍 Checking doctor {} access to medical record {}", doctorUserId, id);

        boolean canAccess = dossierMedicalService.canDoctorAccessDossier(doctorUserId, id);
        return ResponseEntity.ok(Map.of("canAccess", canAccess));
    }

    /**
     * Get medical record statistics
     * GET /api/v1/dossiers-medicaux/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DossierStats> getDossierStats() {
        logger.info("📊 Fetching medical record statistics");

        DossierStats stats = dossierMedicalService.getDossierStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Count medical records by doctor
     * GET /api/v1/dossiers-medicaux/doctor/{medecinUserId}/count
     */
    @GetMapping("/doctor/{medecinUserId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Long>> countDossiersByDoctor(@PathVariable Long medecinUserId) {
        logger.info("📊 Counting medical records for doctor: {}", medecinUserId);

        long count = dossierMedicalService.countByMedecin(medecinUserId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Health check endpoint
     * GET /api/v1/dossiers-medicaux/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "DossierMedicalService",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
package com.cabinet360.core.controller;

import com.cabinet360.core.dto.OrdonnanceDto;
import com.cabinet360.core.service.OrdonnanceService;
import com.cabinet360.core.service.OrdonnanceService.PrescriptionStats;
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
 * REST Controller for Medical Prescriptions (Ordonnance) Management
 * Handles all prescription-related operations including CRUD, search, and analytics
 */
@RestController
@RequestMapping("/api/v1/prescriptions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class OrdonnanceController {

    private static final Logger logger = LoggerFactory.getLogger(OrdonnanceController.class);

    private final OrdonnanceService ordonnanceService;

    public OrdonnanceController(OrdonnanceService ordonnanceService) {
        this.ordonnanceService = ordonnanceService;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Create a new medical prescription
     * POST /api/v1/prescriptions
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<OrdonnanceDto> createPrescription(@Valid @RequestBody OrdonnanceDto ordonnanceDto) {
        logger.info("💊 Creating new prescription for patient: {} by doctor: {}",
                ordonnanceDto.getPatientUserId(), ordonnanceDto.getMedecinUserId());

        try {
            OrdonnanceDto created = ordonnanceService.createOrdonnance(ordonnanceDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Failed to create prescription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create multiple prescriptions in batch
     * POST /api/v1/prescriptions/batch
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<OrdonnanceDto>> createPrescriptionsBatch(@Valid @RequestBody List<OrdonnanceDto> prescriptions) {
        logger.info("💊 Creating {} prescriptions in batch", prescriptions.size());

        try {
            List<OrdonnanceDto> created = ordonnanceService.createOrdonnancesBatch(prescriptions);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("❌ Failed to create prescriptions in batch: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Get prescription by ID
     * GET /api/v1/prescriptions/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<OrdonnanceDto> getPrescriptionById(@PathVariable Long id) {
        logger.info("🔍 Fetching prescription: {}", id);

        try {
            OrdonnanceDto prescription = ordonnanceService.findById(id);
            return ResponseEntity.ok(prescription);
        } catch (Exception e) {
            logger.error("❌ Prescription not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all prescriptions with pagination
     * GET /api/v1/prescriptions?page=0&size=10&sortBy=dateOrdonnance&sortDirection=DESC
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<OrdonnanceDto>> getAllPrescriptions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateOrdonnance") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        logger.info("🔍 Fetching all prescriptions - page: {}, size: {}", page, size);

        Page<OrdonnanceDto> prescriptions = ordonnanceService.findAll(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get prescriptions by medical record (dossier medical)
     * GET /api/v1/prescriptions/dossier/{dossierMedicalId}
     */
    @GetMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<OrdonnanceDto>> getPrescriptionsByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🔍 Fetching prescriptions for dossier: {}", dossierMedicalId);

        try {
            List<OrdonnanceDto> prescriptions = ordonnanceService.findByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch prescriptions for dossier: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get prescriptions by patient
     * GET /api/v1/prescriptions/patient/{patientUserId}
     */
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<List<OrdonnanceDto>> getPrescriptionsByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching prescriptions for patient: {}", patientUserId);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findByPatient(patientUserId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get prescriptions by doctor
     * GET /api/v1/prescriptions/doctor/{medecinUserId}
     */
    @GetMapping("/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<OrdonnanceDto>> getPrescriptionsByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching prescriptions by doctor: {}", medecinUserId);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findByMedecin(medecinUserId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get prescriptions by doctor and patient combination
     * GET /api/v1/prescriptions/doctor/{medecinUserId}/patient/{patientUserId}
     */
    @GetMapping("/doctor/{medecinUserId}/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<OrdonnanceDto>> getPrescriptionsByDoctorAndPatient(
            @PathVariable Long medecinUserId,
            @PathVariable Long patientUserId) {

        logger.info("🔍 Fetching prescriptions by doctor {} for patient {}", medecinUserId, patientUserId);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findByMedecinAndPatient(medecinUserId, patientUserId);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get prescriptions by patient within date range
     * GET /api/v1/prescriptions/patient/{patientUserId}/date-range?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     */
    @GetMapping("/patient/{patientUserId}/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<List<OrdonnanceDto>> getPrescriptionsByPatientAndDateRange(
            @PathVariable Long patientUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("🔍 Fetching prescriptions for patient {} between {} and {}", patientUserId, startDate, endDate);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findByPatientAndDateRange(patientUserId, startDate, endDate);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get prescriptions by doctor within date range
     * GET /api/v1/prescriptions/doctor/{medecinUserId}/date-range?startDate=...&endDate=...
     */
    @GetMapping("/doctor/{medecinUserId}/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<OrdonnanceDto>> getPrescriptionsByDoctorAndDateRange(
            @PathVariable Long medecinUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("🔍 Fetching prescriptions by doctor {} between {} and {}", medecinUserId, startDate, endDate);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findByMedecinAndDateRange(medecinUserId, startDate, endDate);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get recent prescriptions for a patient
     * GET /api/v1/prescriptions/patient/{patientUserId}/recent?limit=5
     */
    @GetMapping("/patient/{patientUserId}/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<List<OrdonnanceDto>> getRecentPrescriptionsByPatient(
            @PathVariable Long patientUserId,
            @RequestParam(defaultValue = "5") @Min(1) int limit) {

        logger.info("🔍 Fetching {} recent prescriptions for patient: {}", limit, patientUserId);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findRecentPrescriptionsByPatient(patientUserId, limit);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get recent prescriptions by doctor
     * GET /api/v1/prescriptions/doctor/{medecinUserId}/recent?limit=10
     */
    @GetMapping("/doctor/{medecinUserId}/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<OrdonnanceDto>> getRecentPrescriptionsByDoctor(
            @PathVariable Long medecinUserId,
            @RequestParam(defaultValue = "10") @Min(1) int limit) {

        logger.info("🔍 Fetching {} recent prescriptions by doctor: {}", limit, medecinUserId);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findRecentPrescriptionsByMedecin(medecinUserId, limit);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Get today's prescriptions by doctor
     * GET /api/v1/prescriptions/doctor/{medecinUserId}/today
     */
    @GetMapping("/doctor/{medecinUserId}/today")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<OrdonnanceDto>> getTodayPrescriptionsByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching today's prescriptions by doctor: {}", medecinUserId);

        List<OrdonnanceDto> prescriptions = ordonnanceService.findTodayPrescriptionsByMedecin(medecinUserId);
        return ResponseEntity.ok(prescriptions);
    }

    // ========================================
    // SEARCH OPERATIONS
    // ========================================

    /**
     * Search prescriptions by content/medication
     * GET /api/v1/prescriptions/search?keyword=paracetamol
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<OrdonnanceDto>> searchPrescriptionsByContent(@RequestParam @NotBlank String keyword) {
        logger.info("🔍 Searching prescriptions by keyword: {}", keyword);

        List<OrdonnanceDto> prescriptions = ordonnanceService.searchByContent(keyword);
        return ResponseEntity.ok(prescriptions);
    }

    /**
     * Check if prescription contains specific medication
     * GET /api/v1/prescriptions/{id}/contains-medication?medication=paracetamol
     */
    @GetMapping("/{id}/contains-medication")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Boolean>> checkMedicationInPrescription(
            @PathVariable Long id,
            @RequestParam @NotBlank String medication) {

        logger.info("🔍 Checking if prescription {} contains medication: {}", id, medication);

        try {
            OrdonnanceDto prescription = ordonnanceService.findById(id);
            boolean contains = ordonnanceService.containsMedication(prescription.getContenu(), medication);
            return ResponseEntity.ok(Map.of("containsMedication", contains));
        } catch (Exception e) {
            logger.error("❌ Failed to check medication: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Update prescription
     * PUT /api/v1/prescriptions/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<OrdonnanceDto> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody OrdonnanceDto ordonnanceDto) {

        logger.info("🔄 Updating prescription: {}", id);

        try {
            OrdonnanceDto updated = ordonnanceService.updateOrdonnance(id, ordonnanceDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to update prescription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Partially update prescription
     * PATCH /api/v1/prescriptions/{id}
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<OrdonnanceDto> partialUpdatePrescription(
            @PathVariable Long id,
            @RequestBody OrdonnanceDto partialDto) {

        logger.info("🔄 Partially updating prescription: {}", id);

        try {
            OrdonnanceDto updated = ordonnanceService.partialUpdate(id, partialDto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to partially update prescription: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update only prescription content
     * PATCH /api/v1/prescriptions/{id}/content
     */
    @PatchMapping("/{id}/content")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<OrdonnanceDto> updatePrescriptionContent(
            @PathVariable Long id,
            @RequestBody @NotBlank @Size(max = 1500) String content) {

        logger.info("🔄 Updating prescription content for ID: {}", id);

        try {
            OrdonnanceDto updated = ordonnanceService.updatePrescriptionContent(id, content);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            logger.error("❌ Failed to update prescription content: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Delete prescription
     * DELETE /api/v1/prescriptions/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePrescription(@PathVariable Long id) {
        logger.info("🗑️ Deleting prescription: {}", id);

        try {
            ordonnanceService.deleteOrdonnance(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Prescription deleted successfully",
                    "prescriptionId", id.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete prescription: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete multiple prescriptions in batch
     * DELETE /api/v1/prescriptions/batch
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deletePrescriptionsBatch(@RequestBody List<Long> ids) {
        logger.info("🗑️ Deleting {} prescriptions in batch", ids.size());

        try {
            ordonnanceService.deleteOrdonnancesBatch(ids);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Prescriptions deleted successfully",
                    "deletedCount", ids.size(),
                    "prescriptionIds", ids
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete prescriptions in batch: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete all prescriptions for a dossier medical
     * DELETE /api/v1/prescriptions/dossier/{dossierMedicalId}
     */
    @DeleteMapping("/dossier/{dossierMedicalId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePrescriptionsByDossier(@PathVariable Long dossierMedicalId) {
        logger.info("🗑️ Deleting all prescriptions for dossier: {}", dossierMedicalId);

        try {
            ordonnanceService.deleteByDossierMedical(dossierMedicalId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All prescriptions deleted for dossier medical",
                    "dossierMedicalId", dossierMedicalId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete prescriptions for dossier: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete all prescriptions for a patient
     * DELETE /api/v1/prescriptions/patient/{patientUserId}
     */
    @DeleteMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePrescriptionsByPatient(@PathVariable Long patientUserId) {
        logger.info("🗑️ Deleting all prescriptions for patient: {}", patientUserId);

        try {
            ordonnanceService.deleteByPatient(patientUserId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All prescriptions deleted for patient",
                    "patientUserId", patientUserId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete prescriptions for patient: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete all prescriptions by doctor
     * DELETE /api/v1/prescriptions/doctor/{medecinUserId}
     */
    @DeleteMapping("/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePrescriptionsByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🗑️ Deleting all prescriptions by doctor: {}", medecinUserId);

        try {
            ordonnanceService.deleteByMedecin(medecinUserId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "All prescriptions deleted for doctor",
                    "medecinUserId", medecinUserId.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to delete prescriptions by doctor: {}", e.getMessage());
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
     * Get prescription statistics for a doctor
     * GET /api/v1/prescriptions/doctor/{medecinUserId}/stats
     */
    @GetMapping("/doctor/{medecinUserId}/stats")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<PrescriptionStats> getPrescriptionStatsForDoctor(@PathVariable Long medecinUserId) {
        logger.info("📊 Getting prescription statistics for doctor: {}", medecinUserId);

        PrescriptionStats stats = ordonnanceService.getPrescriptionStatsForMedecin(medecinUserId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get prescription statistics for a patient
     * GET /api/v1/prescriptions/patient/{patientUserId}/stats
     */
    @GetMapping("/patient/{patientUserId}/stats")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<PrescriptionStats> getPrescriptionStatsForPatient(@PathVariable Long patientUserId) {
        logger.info("📊 Getting prescription statistics for patient: {}", patientUserId);

        PrescriptionStats stats = ordonnanceService.getPrescriptionStatsForPatient(patientUserId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get prescription count by doctor
     * GET /api/v1/prescriptions/doctor/{medecinUserId}/count
     */
    @GetMapping("/doctor/{medecinUserId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getPrescriptionCountByDoctor(@PathVariable Long medecinUserId) {
        logger.info("📊 Counting prescriptions by doctor: {}", medecinUserId);

        long count = ordonnanceService.countByMedecin(medecinUserId);
        return ResponseEntity.ok(Map.of("prescriptionCount", count));
    }

    /**
     * Get prescription count by patient
     * GET /api/v1/prescriptions/patient/{patientUserId}/count
     */
    @GetMapping("/patient/{patientUserId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (hasRole('PATIENT') and #patientUserId == authentication.principal.id)")
    public ResponseEntity<Map<String, Long>> getPrescriptionCountByPatient(@PathVariable Long patientUserId) {
        logger.info("📊 Counting prescriptions for patient: {}", patientUserId);

        long count = ordonnanceService.countByPatient(patientUserId);
        return ResponseEntity.ok(Map.of("prescriptionCount", count));
    }

    // ========================================
    // UTILITY OPERATIONS
    // ========================================

    /**
     * Check if prescription exists
     * GET /api/v1/prescriptions/{id}/exists
     */
    @GetMapping("/{id}/exists")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Boolean>> checkPrescriptionExists(@PathVariable Long id) {
        logger.info("🔍 Checking if prescription exists: {}", id);

        boolean exists = ordonnanceService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Check if doctor can prescribe to patient
     * GET /api/v1/prescriptions/can-prescribe?medecinUserId=1&patientUserId=1
     */
    @GetMapping("/can-prescribe")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, Boolean>> canDoctorPrescribeToPatient(
            @RequestParam Long medecinUserId,
            @RequestParam Long patientUserId) {

        logger.info("🔍 Checking if doctor {} can prescribe to patient {}", medecinUserId, patientUserId);

        boolean canPrescribe = ordonnanceService.canMedecinPrescribeToPatient(medecinUserId, patientUserId);
        return ResponseEntity.ok(Map.of("canPrescribe", canPrescribe));
    }

    /**
     * Health check endpoint for prescription service
     * GET /api/v1/prescriptions/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "OrdonnanceService",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
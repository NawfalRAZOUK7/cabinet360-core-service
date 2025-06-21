package com.cabinet360.core.controller;

import com.cabinet360.core.dto.PatientCabinetLinkDto;
import com.cabinet360.core.service.PatientCabinetLinkService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Patient-Cabinet Link Management
 * Handles patient-cabinet relationships and access control
 */
@RestController
@RequestMapping("/api/v1/patient-cabinet-links")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PatientCabinetLinkController {

    private static final Logger logger = LoggerFactory.getLogger(PatientCabinetLinkController.class);

    private final PatientCabinetLinkService linkService;

    public PatientCabinetLinkController(PatientCabinetLinkService linkService) {
        this.linkService = linkService;
    }

    /**
     * Create a simple link between patient and cabinet
     * POST /api/v1/patient-cabinet-links/simple
     */
    @PostMapping("/simple")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<PatientCabinetLinkDto> linkPatientToCabinet(
            @RequestParam Long patientUserId,
            @RequestParam Long cabinetId) {
        logger.info("🔗 Creating simple link: patient {} to cabinet {}", patientUserId, cabinetId);

        try {
            PatientCabinetLinkDto link = linkService.linkPatientToCabinet(patientUserId, cabinetId);
            return new ResponseEntity<>(link, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Link creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create a detailed link with custom parameters
     * POST /api/v1/patient-cabinet-links
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<PatientCabinetLinkDto> createLink(@Valid @RequestBody PatientCabinetLinkDto linkDto) {
        logger.info("🔗 Creating detailed link: patient {} to cabinet {}",
                linkDto.getPatientUserId(), linkDto.getCabinetId());

        try {
            PatientCabinetLinkDto created = linkService.createLink(linkDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Detailed link creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get link by ID
     * GET /api/v1/patient-cabinet-links/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<PatientCabinetLinkDto> getLinkById(@PathVariable Long id) {
        logger.info("🔍 Fetching link: {}", id);

        Optional<PatientCabinetLinkDto> link = linkService.getLinkById(id);
        return link.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get specific link between patient and cabinet
     * GET /api/v1/patient-cabinet-links/patient/{patientUserId}/cabinet/{cabinetId}
     */
    @GetMapping("/patient/{patientUserId}/cabinet/{cabinetId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<PatientCabinetLinkDto> getSpecificLink(
            @PathVariable Long patientUserId,
            @PathVariable Long cabinetId) {
        logger.info("🔍 Fetching link: patient {} to cabinet {}", patientUserId, cabinetId);

        Optional<PatientCabinetLinkDto> link = linkService.getLink(patientUserId, cabinetId);
        return link.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all links for a patient
     * GET /api/v1/patient-cabinet-links/patient/{patientUserId}
     */
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<PatientCabinetLinkDto>> getLinksByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching links for patient: {}", patientUserId);

        List<PatientCabinetLinkDto> links = linkService.getLinksByPatient(patientUserId);
        return ResponseEntity.ok(links);
    }

    /**
     * Get active links for a patient
     * GET /api/v1/patient-cabinet-links/patient/{patientUserId}/active
     */
    @GetMapping("/patient/{patientUserId}/active")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<PatientCabinetLinkDto>> getActiveLinksByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching active links for patient: {}", patientUserId);

        List<PatientCabinetLinkDto> links = linkService.getActiveLinksByPatient(patientUserId);
        return ResponseEntity.ok(links);
    }

    /**
     * Get all links for a cabinet
     * GET /api/v1/patient-cabinet-links/cabinet/{cabinetId}
     */
    @GetMapping("/cabinet/{cabinetId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<PatientCabinetLinkDto>> getLinksByCabinet(@PathVariable Long cabinetId) {
        logger.info("🔍 Fetching links for cabinet: {}", cabinetId);

        List<PatientCabinetLinkDto> links = linkService.getLinksByCabinet(cabinetId);
        return ResponseEntity.ok(links);
    }

    /**
     * Get active links for a cabinet
     * GET /api/v1/patient-cabinet-links/cabinet/{cabinetId}/active
     */
    @GetMapping("/cabinet/{cabinetId}/active")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<PatientCabinetLinkDto>> getActiveLinksByCabinet(@PathVariable Long cabinetId) {
        logger.info("🔍 Fetching active links for cabinet: {}", cabinetId);

        List<PatientCabinetLinkDto> links = linkService.getActiveLinksByCabinet(cabinetId);
        return ResponseEntity.ok(links);
    }

    /**
     * Get pending links requiring approval
     * GET /api/v1/patient-cabinet-links/pending
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PatientCabinetLinkDto>> getPendingLinks() {
        logger.info("🔍 Fetching pending links");

        List<PatientCabinetLinkDto> links = linkService.getPendingLinks();
        return ResponseEntity.ok(links);
    }

    /**
     * Update existing link
     * PUT /api/v1/patient-cabinet-links/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<PatientCabinetLinkDto> updateLink(
            @PathVariable Long id,
            @Valid @RequestBody PatientCabinetLinkDto linkDto) {
        logger.info("🔄 Updating link: {}", id);

        try {
            PatientCabinetLinkDto updated = linkService.updateLink(id, linkDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Link update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Activate link
     * PATCH /api/v1/patient-cabinet-links/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<PatientCabinetLinkDto> activateLink(@PathVariable Long id) {
        logger.info("🟢 Activating link: {}", id);

        try {
            PatientCabinetLinkDto activated = linkService.activateLink(id);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Link activation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deactivate link
     * PATCH /api/v1/patient-cabinet-links/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<PatientCabinetLinkDto> deactivateLink(@PathVariable Long id) {
        logger.info("🔴 Deactivating link: {}", id);

        try {
            PatientCabinetLinkDto deactivated = linkService.deactivateLink(id);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Link deactivation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Revoke link
     * PATCH /api/v1/patient-cabinet-links/{id}/revoke
     */
    @PatchMapping("/{id}/revoke")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<PatientCabinetLinkDto> revokeLink(@PathVariable Long id) {
        logger.info("❌ Revoking link: {}", id);

        try {
            PatientCabinetLinkDto revoked = linkService.revokeLink(id);
            return ResponseEntity.ok(revoked);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Link revocation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Soft delete link
     * DELETE /api/v1/patient-cabinet-links/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteLink(@PathVariable Long id) {
        logger.info("🗑️ Deleting link: {}", id);

        try {
            linkService.deleteLink(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Link deleted successfully",
                    "linkId", id.toString()
            ));
        } catch (IllegalArgumentException e) {
            logger.error("❌ Link deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hard delete link between patient and cabinet
     * DELETE /api/v1/patient-cabinet-links/patient/{patientUserId}/cabinet/{cabinetId}
     */
    @DeleteMapping("/patient/{patientUserId}/cabinet/{cabinetId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> unlinkPatientFromCabinet(
            @PathVariable Long patientUserId,
            @PathVariable Long cabinetId) {
        logger.info("🗑️ Unlinking patient {} from cabinet {}", patientUserId, cabinetId);

        linkService.unlinkPatientFromCabinet(patientUserId, cabinetId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Patient unlinked from cabinet successfully",
                "patientUserId", patientUserId.toString(),
                "cabinetId", cabinetId.toString()
        ));
    }

    /**
     * Update last access time
     * PATCH /api/v1/patient-cabinet-links/patient/{patientUserId}/cabinet/{cabinetId}/access
     */
    @PatchMapping("/patient/{patientUserId}/cabinet/{cabinetId}/access")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<PatientCabinetLinkDto> updateLastAccess(
            @PathVariable Long patientUserId,
            @PathVariable Long cabinetId) {
        logger.info("🕒 Updating last access: patient {} to cabinet {}", patientUserId, cabinetId);

        try {
            PatientCabinetLinkDto updated = linkService.updateLastAccess(patientUserId, cabinetId);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Last access update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if patient is linked to cabinet
     * GET /api/v1/patient-cabinet-links/patient/{patientUserId}/cabinet/{cabinetId}/exists
     */
    @GetMapping("/patient/{patientUserId}/cabinet/{cabinetId}/exists")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, Boolean>> checkLinkExists(
            @PathVariable Long patientUserId,
            @PathVariable Long cabinetId) {
        logger.info("🔍 Checking link existence: patient {} to cabinet {}", patientUserId, cabinetId);

        boolean exists = linkService.isPatientLinkedToCabinet(patientUserId, cabinetId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Count active links for patient
     * GET /api/v1/patient-cabinet-links/patient/{patientUserId}/count
     */
    @GetMapping("/patient/{patientUserId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, Long>> countLinksForPatient(@PathVariable Long patientUserId) {
        logger.info("📊 Counting active links for patient: {}", patientUserId);

        long count = linkService.countActiveLinksForPatient(patientUserId);
        return ResponseEntity.ok(Map.of("activeLinksCount", count));
    }

    /**
     * Count active links for cabinet
     * GET /api/v1/patient-cabinet-links/cabinet/{cabinetId}/count
     */
    @GetMapping("/cabinet/{cabinetId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Long>> countLinksForCabinet(@PathVariable Long cabinetId) {
        logger.info("📊 Counting active links for cabinet: {}", cabinetId);

        long count = linkService.countActiveLinksForCabinet(cabinetId);
        return ResponseEntity.ok(Map.of("activeLinksCount", count));
    }

    /**
     * Bulk operations - Link multiple patients to a cabinet
     * POST /api/v1/patient-cabinet-links/bulk/cabinet/{cabinetId}
     */
    @PostMapping("/bulk/cabinet/{cabinetId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkLinkPatientsToCabinet(
            @PathVariable Long cabinetId,
            @RequestBody List<Long> patientUserIds) {
        logger.info("🔗 Bulk linking {} patients to cabinet {}", patientUserIds.size(), cabinetId);

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new java.util.ArrayList<>();

        for (Long patientUserId : patientUserIds) {
            try {
                linkService.linkPatientToCabinet(patientUserId, cabinetId);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errors.add("Patient " + patientUserId + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = Map.of(
                "totalRequested", patientUserIds.size(),
                "successCount", successCount,
                "failureCount", failureCount,
                "errors", errors
        );

        logger.info("✅ Bulk linking completed: {} success, {} failures", successCount, failureCount);
        return ResponseEntity.ok(result);
    }

    /**
     * Bulk operations - Activate multiple links
     * PATCH /api/v1/patient-cabinet-links/bulk/activate
     */
    @PatchMapping("/bulk/activate")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkActivateLinks(@RequestBody List<Long> linkIds) {
        logger.info("🟢 Bulk activating {} links", linkIds.size());

        int successCount = 0;
        int failureCount = 0;
        List<String> errors = new java.util.ArrayList<>();

        for (Long linkId : linkIds) {
            try {
                linkService.activateLink(linkId);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                errors.add("Link " + linkId + ": " + e.getMessage());
            }
        }

        Map<String, Object> result = Map.of(
                "totalRequested", linkIds.size(),
                "successCount", successCount,
                "failureCount", failureCount,
                "errors", errors
        );

        logger.info("✅ Bulk activation completed: {} success, {} failures", successCount, failureCount);
        return ResponseEntity.ok(result);
    }

    /**
     * Statistics endpoint
     * GET /api/v1/patient-cabinet-links/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getLinkStatistics() {
        logger.info("📊 Fetching link statistics");

        // This would be implemented in the service layer
        Map<String, Object> stats = Map.of(
                "totalLinks", 0L, // linkService.countAllLinks(),
                "activeLinks", 0L, // linkService.countActiveLinks(),
                "pendingLinks", 0L, // linkService.countPendingLinks(),
                "revokedLinks", 0L, // linkService.countRevokedLinks(),
                "lastUpdated", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint
     * GET /api/v1/patient-cabinet-links/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "PatientCabinetLinkService",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
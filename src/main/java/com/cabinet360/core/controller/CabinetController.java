package com.cabinet360.core.controller;

import com.cabinet360.core.dto.CabinetDto;
import com.cabinet360.core.dto.CabinetSettingsDto;
import com.cabinet360.core.service.CabinetService;
import com.cabinet360.core.service.CabinetService.CabinetStatsDto;
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
 * REST Controller for Cabinet Management
 * Handles all cabinet-related operations including CRUD, settings, and statistics
 */
@RestController
@RequestMapping("/api/v1/cabinets")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CabinetController {

    private static final Logger logger = LoggerFactory.getLogger(CabinetController.class);

    private final CabinetService cabinetService;

    public CabinetController(CabinetService cabinetService) {
        this.cabinetService = cabinetService;
    }

    /**
     * Create a new medical cabinet
     * POST /api/v1/cabinets
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<CabinetDto> createCabinet(@Valid @RequestBody CabinetDto cabinetDto) {
        logger.info("🏥 Creating new cabinet: {}", cabinetDto.getCabinetName());

        try {
            CabinetDto created = cabinetService.createCabinet(cabinetDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Cabinet creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get cabinet by ID
     * GET /api/v1/cabinets/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<CabinetDto> getCabinetById(@PathVariable Long id) {
        logger.info("🔍 Fetching cabinet: {}", id);

        Optional<CabinetDto> cabinet = cabinetService.getCabinetById(id);
        return cabinet.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all cabinets owned by a specific doctor
     * GET /api/v1/cabinets/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CabinetDto>> getCabinetsByDoctor(@PathVariable Long doctorId) {
        logger.info("🔍 Fetching cabinets for doctor: {}", doctorId);

        List<CabinetDto> cabinets = cabinetService.getCabinetsByDoctor(doctorId);
        return ResponseEntity.ok(cabinets);
    }

    /**
     * Get active cabinets owned by a specific doctor
     * GET /api/v1/cabinets/doctor/{doctorId}/active
     */
    @GetMapping("/doctor/{doctorId}/active")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<List<CabinetDto>> getActiveCabinetsByDoctor(@PathVariable Long doctorId) {
        logger.info("🔍 Fetching active cabinets for doctor: {}", doctorId);

        List<CabinetDto> cabinets = cabinetService.getActiveCabinetsByDoctor(doctorId);
        return ResponseEntity.ok(cabinets);
    }

    /**
     * Get all active cabinets
     * GET /api/v1/cabinets/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT')")
    public ResponseEntity<List<CabinetDto>> getAllActiveCabinets() {
        logger.info("🔍 Fetching all active cabinets");

        List<CabinetDto> cabinets = cabinetService.getAllActiveCabinets();
        return ResponseEntity.ok(cabinets);
    }

    /**
     * Search cabinets by name
     * GET /api/v1/cabinets/search?name={name}
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT')")
    public ResponseEntity<List<CabinetDto>> searchCabinetsByName(@RequestParam String name) {
        logger.info("🔍 Searching cabinets by name: {}", name);

        List<CabinetDto> cabinets = cabinetService.searchCabinetsByName(name);
        return ResponseEntity.ok(cabinets);
    }

    /**
     * Get cabinets with online booking enabled
     * GET /api/v1/cabinets/online-booking
     */
    @GetMapping("/online-booking")
    public ResponseEntity<List<CabinetDto>> getCabinetsWithOnlineBooking() {
        logger.info("🔍 Fetching cabinets with online booking");

        List<CabinetDto> cabinets = cabinetService.getCabinetsWithOnlineBooking();
        return ResponseEntity.ok(cabinets);
    }

    /**
     * Update cabinet information
     * PUT /api/v1/cabinets/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<CabinetDto> updateCabinet(
            @PathVariable Long id,
            @Valid @RequestBody CabinetDto cabinetDto) {
        logger.info("🔄 Updating cabinet: {}", id);

        try {
            CabinetDto updated = cabinetService.updateCabinet(id, cabinetDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Cabinet update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update cabinet settings only
     * PUT /api/v1/cabinets/{id}/settings
     */
    @PutMapping("/{id}/settings")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<CabinetDto> updateCabinetSettings(
            @PathVariable Long id,
            @Valid @RequestBody CabinetSettingsDto settingsDto) {
        logger.info("⚙️ Updating cabinet settings: {}", id);

        try {
            CabinetDto updated = cabinetService.updateCabinetSettings(id, settingsDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Cabinet settings update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Activate cabinet
     * PATCH /api/v1/cabinets/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<CabinetDto> activateCabinet(@PathVariable Long id) {
        logger.info("🟢 Activating cabinet: {}", id);

        try {
            CabinetDto activated = cabinetService.activateCabinet(id);
            return ResponseEntity.ok(activated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Cabinet activation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deactivate cabinet
     * PATCH /api/v1/cabinets/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<CabinetDto> deactivateCabinet(@PathVariable Long id) {
        logger.info("🔴 Deactivating cabinet: {}", id);

        try {
            CabinetDto deactivated = cabinetService.deactivateCabinet(id);
            return ResponseEntity.ok(deactivated);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Cabinet deactivation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Suspend cabinet
     * PATCH /api/v1/cabinets/{id}/suspend
     */
    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CabinetDto> suspendCabinet(@PathVariable Long id) {
        logger.info("⏸️ Suspending cabinet: {}", id);

        try {
            CabinetDto suspended = cabinetService.suspendCabinet(id);
            return ResponseEntity.ok(suspended);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Cabinet suspension failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Soft delete cabinet
     * DELETE /api/v1/cabinets/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteCabinet(@PathVariable Long id) {
        logger.info("🗑️ Deleting cabinet: {}", id);

        try {
            cabinetService.deleteCabinet(id);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Cabinet deleted successfully",
                    "cabinetId", id.toString()
            ));
        } catch (IllegalArgumentException e) {
            logger.error("❌ Cabinet deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Check if doctor owns cabinet
     * GET /api/v1/cabinets/{id}/ownership/{doctorId}
     */
    @GetMapping("/{id}/ownership/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkOwnership(
            @PathVariable Long id,
            @PathVariable Long doctorId) {
        logger.info("🔍 Checking ownership: cabinet {} for doctor {}", id, doctorId);

        boolean isOwner = cabinetService.isDoctorOwner(doctorId, id);
        return ResponseEntity.ok(Map.of("isOwner", isOwner));
    }

    /**
     * Get cabinet statistics
     * GET /api/v1/cabinets/{id}/stats
     */
    @GetMapping("/{id}/stats")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<CabinetStatsDto> getCabinetStats(@PathVariable Long id) {
        logger.info("📊 Fetching cabinet statistics: {}", id);

        try {
            CabinetStatsDto stats = cabinetService.getCabinetStats(id);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Failed to get cabinet stats: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Count active cabinets for a doctor
     * GET /api/v1/cabinets/doctor/{doctorId}/count
     */
    @GetMapping("/doctor/{doctorId}/count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countActiveCabinetsByDoctor(@PathVariable Long doctorId) {
        logger.info("📊 Counting active cabinets for doctor: {}", doctorId);

        long count = cabinetService.countActiveCabinetsByDoctor(doctorId);
        return ResponseEntity.ok(Map.of("activeCabinetsCount", count));
    }

    /**
     * Health check endpoint for cabinet service
     * GET /api/v1/cabinets/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "CabinetService",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
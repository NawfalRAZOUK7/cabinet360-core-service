package com.cabinet360.core.controller;

import com.cabinet360.core.dto.RendezVousDto;
import com.cabinet360.core.enums.RendezVousStatut;
import com.cabinet360.core.service.RendezVousService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Appointment Management (RendezVous)
 * Handles appointment scheduling, modifications, and cancellations with conflict detection
 */
@RestController
@RequestMapping("/api/v1/rendez-vous")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RendezVousController {

    private static final Logger logger = LoggerFactory.getLogger(RendezVousController.class);

    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Create a new appointment
     * POST /api/v1/rendez-vous
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> createRendezVous(@Valid @RequestBody RendezVousDto rendezVousDto) {
        logger.info("📅 Creating appointment for patient: {} with doctor: {} at: {}",
                rendezVousDto.getPatientUserId(), rendezVousDto.getMedecinUserId(), rendezVousDto.getDateHeure());

        try {
            RendezVousDto created = rendezVousService.createRendezVous(rendezVousDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            logger.error("❌ Appointment conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Type", "SCHEDULING_CONFLICT")
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Get appointment by ID
     * GET /api/v1/rendez-vous/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> getRendezVousById(@PathVariable Long id) {
        logger.info("🔍 Fetching appointment: {}", id);

        try {
            RendezVousDto rendezVous = rendezVousService.findById(id);
            return ResponseEntity.ok(rendezVous);
        } catch (Exception e) {
            logger.error("❌ Appointment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all appointments
     * GET /api/v1/rendez-vous
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<RendezVousDto>> getAllRendezVous() {
        logger.info("📋 Fetching all appointments");

        List<RendezVousDto> appointments = rendezVousService.listAllRendezVous();
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by patient
     * GET /api/v1/rendez-vous/patient/{patientUserId}
     */
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching appointments for patient: {}", patientUserId);

        List<RendezVousDto> appointments = rendezVousService.findByPatientUserId(patientUserId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by doctor
     * GET /api/v1/rendez-vous/doctor/{medecinUserId}
     */
    @GetMapping("/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching appointments for doctor: {}", medecinUserId);

        List<RendezVousDto> appointments = rendezVousService.findByMedecinUserId(medecinUserId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by status
     * GET /api/v1/rendez-vous/status/{statut}
     */
    @GetMapping("/status/{statut}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByStatus(@PathVariable RendezVousStatut statut) {
        logger.info("🔍 Fetching appointments with status: {}", statut);

        List<RendezVousDto> appointments = rendezVousService.findByStatut(statut);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by date
     * GET /api/v1/rendez-vous/date/{date}
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        logger.info("🔍 Fetching appointments for date: {}", date);

        List<RendezVousDto> appointments = rendezVousService.findByDateHeure(date);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by date range
     * GET /api/v1/rendez-vous/date-range?start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        logger.info("🔍 Fetching appointments between {} and {}", start, end);

        List<RendezVousDto> appointments = rendezVousService.findByDateRange(start, end);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get patient appointments in date range
     * GET /api/v1/rendez-vous/patient/{patientUserId}/date-range?start=...&end=...
     */
    @GetMapping("/patient/{patientUserId}/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<RendezVousDto>> getPatientAppointmentsByDateRange(
            @PathVariable Long patientUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        logger.info("🔍 Fetching appointments for patient {} between {} and {}", patientUserId, start, end);

        List<RendezVousDto> appointments = rendezVousService.findByPatientUserIdAndDateHeureBetween(patientUserId, start, end);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get doctor appointments for specific date
     * GET /api/v1/rendez-vous/doctor/{medecinUserId}/date/{date}
     */
    @GetMapping("/doctor/{medecinUserId}/date/{date}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getDoctorAppointmentsByDate(
            @PathVariable Long medecinUserId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        logger.info("🔍 Fetching appointments for doctor {} on date: {}", medecinUserId, date);

        List<RendezVousDto> appointments = rendezVousService.findByMedecinUserIdAndDateHeure(medecinUserId, date);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get upcoming appointments for a patient (next 7 days)
     * GET /api/v1/rendez-vous/patient/{patientUserId}/upcoming
     */
    @GetMapping("/patient/{patientUserId}/upcoming")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<RendezVousDto>> getUpcomingPatientAppointments(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching upcoming appointments for patient: {}", patientUserId);

        List<RendezVousDto> appointments = rendezVousService.findUpcomingAppointmentsForPatient(patientUserId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get today's appointments for a doctor
     * GET /api/v1/rendez-vous/doctor/{medecinUserId}/today
     */
    @GetMapping("/doctor/{medecinUserId}/today")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getTodayDoctorAppointments(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching today's appointments for doctor: {}", medecinUserId);

        List<RendezVousDto> appointments = rendezVousService.findTodayAppointmentsForDoctor(medecinUserId);
        return ResponseEntity.ok(appointments);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Update appointment
     * PUT /api/v1/rendez-vous/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> updateRendezVous(
            @PathVariable Long id,
            @Valid @RequestBody RendezVousDto rendezVousDto,
            Authentication authentication) {
        logger.info("🔄 Updating appointment: {}", id);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            String userRole = extractRoleFromAuth(authentication);

            RendezVousDto updated = rendezVousService.updateRendezVous(id, rendezVousDto, requestingUserId, userRole);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for appointment update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            logger.error("❌ Appointment conflict during update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Type", "SCHEDULING_CONFLICT")
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update appointment status
     * PATCH /api/v1/rendez-vous/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<RendezVousDto> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam RendezVousStatut status,
            Authentication authentication) {
        logger.info("🔄 Updating appointment {} status to: {}", id, status);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            RendezVousDto updated = rendezVousService.updateStatus(id, status, requestingUserId);
            return ResponseEntity.ok(updated);
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for status update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("❌ Status update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Confirm appointment
     * PATCH /api/v1/rendez-vous/{id}/confirm
     */
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> confirmAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("✅ Confirming appointment: {}", id);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            RendezVousDto confirmed = rendezVousService.confirmAppointment(id, requestingUserId);
            return ResponseEntity.ok(confirmed);
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for appointment confirmation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("❌ Appointment confirmation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reschedule appointment
     * PATCH /api/v1/rendez-vous/{id}/reschedule
     */
    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> rescheduleAppointment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDateTime,
            @RequestParam(required = false) Integer newDuration,
            Authentication authentication) {
        logger.info("🔄 Rescheduling appointment {} to: {}", id, newDateTime);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            String userRole = extractRoleFromAuth(authentication);

            RendezVousDto rescheduled = rendezVousService.rescheduleAppointment(id, newDateTime, newDuration, requestingUserId, userRole);
            return ResponseEntity.ok(rescheduled);
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for appointment rescheduling: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            logger.error("❌ Rescheduling conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Type", "SCHEDULING_CONFLICT")
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment rescheduling failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Cancel appointment
     * DELETE /api/v1/rendez-vous/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, String>> cancelRendezVous(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("❌ Canceling appointment: {}", id);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            rendezVousService.cancelRendezVous(id, requestingUserId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Appointment canceled successfully",
                    "appointmentId", id.toString()
            ));
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for appointment cancellation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("❌ Appointment cancellation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // UTILITY ENDPOINTS
    // ========================================

    /**
     * Check for scheduling conflicts
     * POST /api/v1/rendez-vous/check-conflicts
     */
    @PostMapping("/check-conflicts")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> checkSchedulingConflicts(@RequestBody RendezVousDto rendezVousDto) {
        logger.info("🔍 Checking conflicts for appointment: doctor={}, patient={}, time={}",
                rendezVousDto.getMedecinUserId(), rendezVousDto.getPatientUserId(), rendezVousDto.getDateHeure());

        try {
            Map<String, Object> conflicts = rendezVousService.checkConflicts(rendezVousDto);
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            logger.error("❌ Conflict check failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get available time slots for a doctor on a specific date
     * GET /api/v1/rendez-vous/doctor/{medecinUserId}/available-slots?date=2024-01-15&duration=30
     */
    @GetMapping("/doctor/{medecinUserId}/available-slots")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<LocalDateTime>> getAvailableTimeSlots(
            @PathVariable Long medecinUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,
            @RequestParam(defaultValue = "30") Integer durationMinutes) {
        logger.info("🔍 Finding available slots for doctor {} on date: {} (duration: {}min)",
                medecinUserId, date, durationMinutes);

        try {
            List<LocalDateTime> availableSlots = rendezVousService.findAvailableTimeSlots(medecinUserId, date, durationMinutes);
            return ResponseEntity.ok(availableSlots);
        } catch (Exception e) {
            logger.error("❌ Available slots lookup failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get appointment statistics
     * GET /api/v1/rendez-vous/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAppointmentStatistics() {
        logger.info("📊 Fetching appointment statistics");

        try {
            Map<String, Object> stats = rendezVousService.getAppointmentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Statistics retrieval failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get doctor's appointment statistics
     * GET /api/v1/rendez-vous/doctor/{medecinUserId}/stats
     */
    @GetMapping("/doctor/{medecinUserId}/stats")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> getDoctorAppointmentStats(@PathVariable Long medecinUserId) {
        logger.info("📊 Fetching appointment statistics for doctor: {}", medecinUserId);

        try {
            Map<String, Object> stats = rendezVousService.getDoctorAppointmentStats(medecinUserId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Doctor statistics retrieval failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/rendez-vous/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RendezVousService",
                "timestamp", System.currentTimeMillis()
        ));
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Extract user ID from JWT authentication
     */
    private Long extractUserIdFromAuth(Authentication authentication) {
        // This would extract the user ID from the JWT token
        // Implementation depends on how the JWT is structured
        // For now, returning a placeholder
        return 1L; // TODO: Implement proper JWT user ID extraction
    }

    /**
     * Extract user role from JWT authentication
     */
    private String extractRoleFromAuth(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(role -> role.startsWith("ROLE_"))
                .findFirst()
                .map(role -> role.substring(5)) // Remove "ROLE_" prefix
                .orElse("UNKNOWN");
    }
}
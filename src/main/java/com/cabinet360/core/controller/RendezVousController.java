package com.cabinet360.core.controller;

import com.cabinet360.core.dto.RendezVousDto;
import com.cabinet360.core.enums.RendezVousStatut;
import com.cabinet360.core.service.RendezVousService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Appointment Management (RendezVous)
 * Handles appointment scheduling, modifications, and cancellations with conflict detection
 */
@RestController
@RequestMapping("/api/v1/rendez-vous")
@CrossOrigin(origins = "*", maxAge = 3600)
@Validated
@Tag(name = "Rendez-Vous Management", description = "APIs for managing medical appointments")
public class RendezVousController {

    private static final Logger logger = LoggerFactory.getLogger(RendezVousController.class);

    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    @Operation(summary = "Create a new appointment", description = "Creates a new medical appointment with conflict detection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Appointment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid appointment data"),
            @ApiResponse(responseCode = "409", description = "Scheduling conflict detected"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
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
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid appointment data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error-Type", "VALIDATION_ERROR")
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "INTERNAL_ERROR")
                    .build();
        }
    }

    @Operation(summary = "Create appointment with slot validation", description = "Creates appointment with advanced slot availability checking")
    @PostMapping("/with-validation")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> createRendezVousWithValidation(@Valid @RequestBody RendezVousDto rendezVousDto) {
        logger.info("📅 Creating appointment with slot validation for patient: {} with doctor: {}",
                rendezVousDto.getPatientUserId(), rendezVousDto.getMedecinUserId());

        try {
            RendezVousDto created = rendezVousService.createRendezVousWithSlotValidation(rendezVousDto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            logger.error("❌ Slot not available: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Type", "SLOT_UNAVAILABLE")
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    @Operation(summary = "Get appointment by ID", description = "Retrieves a specific appointment by its ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> getRendezVousById(
            @Parameter(description = "Appointment ID") @PathVariable Long id) {
        logger.info("🔍 Fetching appointment: {}", id);

        try {
            RendezVousDto rendezVous = rendezVousService.findById(id);
            return ResponseEntity.ok(rendezVous);
        } catch (IllegalArgumentException e) {
            logger.error("❌ Appointment not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("❌ Error fetching appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all appointments with pagination", description = "Retrieves all appointments with pagination support")
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<Page<RendezVousDto>> getAllRendezVous(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "dateHeure") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDir) {

        logger.info("📋 Fetching all appointments - page: {}, size: {}", page, size);

        try {
            Sort.Direction direction = "DESC".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            Page<RendezVousDto> appointments = rendezVousService.listAllRendezVous(pageable);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("❌ Error fetching appointments: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get all appointments (no pagination)", description = "Retrieves all appointments without pagination")
    @GetMapping("/all")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<RendezVousDto>> getAllRendezVousNoPagination() {
        logger.info("📋 Fetching all appointments without pagination");

        List<RendezVousDto> appointments = rendezVousService.listAllRendezVous();
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get appointments by patient", description = "Retrieves all appointments for a specific patient")
    @GetMapping("/patient/{patientUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByPatient(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching appointments for patient: {}", patientUserId);

        List<RendezVousDto> appointments = rendezVousService.findByPatientUserId(patientUserId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get appointments by doctor", description = "Retrieves all appointments for a specific doctor")
    @GetMapping("/doctor/{medecinUserId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByDoctor(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching appointments for doctor: {}", medecinUserId);

        List<RendezVousDto> appointments = rendezVousService.findByMedecinUserId(medecinUserId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get appointments by status", description = "Retrieves appointments filtered by status")
    @GetMapping("/status/{statut}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByStatus(@PathVariable RendezVousStatut statut) {
        logger.info("🔍 Fetching appointments with status: {}", statut);

        List<RendezVousDto> appointments = rendezVousService.findByStatut(statut);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Search appointments by reason", description = "Search appointments by appointment reason (partial match)")
    @GetMapping("/search")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> searchAppointmentsByReason(
            @Parameter(description = "Search term for appointment reason") @RequestParam String motif) {
        logger.info("🔍 Searching appointments with reason containing: {}", motif);

        List<RendezVousDto> appointments = rendezVousService.findByMotifContaining(motif);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get appointments by date", description = "Retrieves appointments for a specific date")
    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        logger.info("🔍 Fetching appointments for date: {}", date);

        List<RendezVousDto> appointments = rendezVousService.findByDateHeure(date);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get appointments by date range", description = "Retrieves appointments within a date range")
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getRendezVousByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        logger.info("🔍 Fetching appointments between {} and {}", start, end);

        List<RendezVousDto> appointments = rendezVousService.findByDateRange(start, end);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get patient appointments in date range")
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

    @Operation(summary = "Get doctor appointments for specific date")
    @GetMapping("/doctor/{medecinUserId}/date/{date}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getDoctorAppointmentsByDate(
            @PathVariable Long medecinUserId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        logger.info("🔍 Fetching appointments for doctor {} on date: {}", medecinUserId, date);

        List<RendezVousDto> appointments = rendezVousService.findByMedecinUserIdAndDateHeure(medecinUserId, date);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get upcoming appointments for a patient")
    @GetMapping("/patient/{patientUserId}/upcoming")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<List<RendezVousDto>> getUpcomingPatientAppointments(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching upcoming appointments for patient: {}", patientUserId);

        List<RendezVousDto> appointments = rendezVousService.findUpcomingAppointmentsForPatient(patientUserId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get today's appointments for a doctor")
    @GetMapping("/doctor/{medecinUserId}/today")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getTodayDoctorAppointments(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching today's appointments for doctor: {}", medecinUserId);

        List<RendezVousDto> appointments = rendezVousService.findTodayAppointmentsForDoctor(medecinUserId);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get next appointment for patient")
    @GetMapping("/patient/{patientUserId}/next")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<RendezVousDto> getNextPatientAppointment(@PathVariable Long patientUserId) {
        logger.info("🔍 Fetching next appointment for patient: {}", patientUserId);

        Optional<RendezVousDto> nextAppointment = rendezVousService.findNextAppointmentForPatient(patientUserId);
        return nextAppointment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get next appointment for doctor")
    @GetMapping("/doctor/{medecinUserId}/next")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<RendezVousDto> getNextDoctorAppointment(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching next appointment for doctor: {}", medecinUserId);

        Optional<RendezVousDto> nextAppointment = rendezVousService.findNextAppointmentForDoctor(medecinUserId);
        return nextAppointment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Get active appointments for doctor")
    @GetMapping("/doctor/{medecinUserId}/active")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<List<RendezVousDto>> getActiveDoctorAppointments(@PathVariable Long medecinUserId) {
        logger.info("🔍 Fetching active appointments for doctor: {}", medecinUserId);

        List<RendezVousDto> appointments = rendezVousService.findActiveAppointmentsByDoctor(medecinUserId);
        return ResponseEntity.ok(appointments);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    @Operation(summary = "Update appointment", description = "Updates an existing appointment with conflict detection")
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (IllegalStateException e) {
            logger.error("❌ Appointment conflict during update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .header("X-Error-Type", "SCHEDULING_CONFLICT")
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid update data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment update failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update appointment status")
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
        } catch (IllegalStateException e) {
            logger.error("❌ Invalid status transition: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("❌ Status update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Confirm appointment")
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

    @Operation(summary = "Start appointment")
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<RendezVousDto> startAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("▶️ Starting appointment: {}", id);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            RendezVousDto started = rendezVousService.startAppointment(id, requestingUserId);
            return ResponseEntity.ok(started);
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for starting appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("❌ Failed to start appointment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Complete appointment")
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<RendezVousDto> completeAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        logger.info("🏁 Completing appointment: {}", id);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            RendezVousDto completed = rendezVousService.completeAppointment(id, requestingUserId, notes);
            return ResponseEntity.ok(completed);
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for completing appointment: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("❌ Failed to complete appointment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Reschedule appointment")
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
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment rescheduling failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    @Operation(summary = "Cancel appointment")
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
                    "appointmentId", id.toString(),
                    "timestamp", String.valueOf(System.currentTimeMillis())
            ));
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for appointment cancellation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalStateException e) {
            logger.error("❌ Cannot cancel appointment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .header("X-Error-Message", e.getMessage())
                    .build();
        } catch (Exception e) {
            logger.error("❌ Appointment cancellation failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Permanently delete appointment (Admin only)")
    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAppointmentPermanently(
            @PathVariable Long id,
            Authentication authentication) {
        logger.warn("🗑️ Permanent deletion requested for appointment: {}", id);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            String userRole = extractRoleFromAuth(authentication);

            rendezVousService.deleteRendezVousPermanently(id, requestingUserId, userRole);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Appointment permanently deleted",
                    "appointmentId", id.toString(),
                    "deletedBy", requestingUserId.toString()
            ));
        } catch (SecurityException e) {
            logger.error("🚫 Access denied for permanent deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("❌ Permanent deletion failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // UTILITY ENDPOINTS
    // ========================================

    @Operation(summary = "Check for scheduling conflicts")
    @PostMapping("/check-conflicts")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> checkSchedulingConflicts(@RequestBody RendezVousDto rendezVousDto) {
        logger.info("🔍 Checking conflicts for appointment: doctor={}, patient={}, time={}",
                rendezVousDto.getMedecinUserId(), rendezVousDto.getPatientUserId(), rendezVousDto.getDateHeure());

        try {
            Map<String, Object> conflicts = rendezVousService.checkConflicts(rendezVousDto);
            return ResponseEntity.ok(conflicts);
        } catch (IllegalStateException e) {
            // Return conflict information instead of error
            Map<String, Object> conflictInfo = Map.of(
                    "hasConflicts", true,
                    "conflictMessage", e.getMessage(),
                    "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(conflictInfo);
        } catch (Exception e) {
            logger.error("❌ Conflict check failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get available time slots for a doctor")
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

    @Operation(summary = "Get formatted available time slots")
    @GetMapping("/doctor/{medecinUserId}/available-slots/formatted")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> getFormattedAvailableTimeSlots(
            @PathVariable Long medecinUserId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date,
            @RequestParam(defaultValue = "30") Integer durationMinutes) {
        logger.info("🔍 Finding formatted available slots for doctor {} on date: {}", medecinUserId, date);

        try {
            Map<String, Object> formattedSlots = rendezVousService.getFormattedAvailableSlots(medecinUserId, date, durationMinutes);
            return ResponseEntity.ok(formattedSlots);
        } catch (Exception e) {
            logger.error("❌ Formatted slots lookup failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // STATISTICS & DASHBOARD ENDPOINTS
    // ========================================

    @Operation(summary = "Get global appointment statistics")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAppointmentStatistics() {
        logger.info("📊 Fetching global appointment statistics");

        try {
            Map<String, Object> stats = rendezVousService.getAppointmentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("❌ Statistics retrieval failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get doctor's appointment statistics")
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

    @Operation(summary = "Get doctor's dashboard")
    @GetMapping("/doctor/{medecinUserId}/dashboard")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> getDoctorDashboard(@PathVariable Long medecinUserId) {
        logger.info("📋 Fetching dashboard for doctor: {}", medecinUserId);

        try {
            Map<String, Object> dashboard = rendezVousService.getDoctorDashboard(medecinUserId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            logger.error("❌ Dashboard retrieval failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // MAINTENANCE ENDPOINTS
    // ========================================

    @Operation(summary = "Cleanup old cancelled appointments (Admin only)")
    @PostMapping("/maintenance/cleanup-cancelled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupOldCancelledAppointments(
            @RequestParam(defaultValue = "90") int daysOld) {
        logger.info("🧹 Starting cleanup of cancelled appointments older than {} days", daysOld);

        try {
            int deletedCount = rendezVousService.cleanupOldCancelledAppointments(daysOld);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Cleanup completed successfully",
                    "deletedAppointments", deletedCount,
                    "daysOld", daysOld,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            logger.error("❌ Cleanup failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Archive old completed appointments (Admin only)")
    @PostMapping("/maintenance/archive-completed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> archiveOldCompletedAppointments(
            @RequestParam(defaultValue = "365") int daysOld) {
        logger.info("📦 Starting archival of completed appointments older than {} days", daysOld);

        try {
            int archivedCount = rendezVousService.archiveOldCompletedAppointments(daysOld);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Archival completed successfully",
                    "archivedAppointments", archivedCount,
                    "daysOld", daysOld,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            logger.error("❌ Archival failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========================================
    // BULK OPERATIONS
    // ========================================

    @Operation(summary = "Bulk update appointment status")
    @PatchMapping("/bulk/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> bulkUpdateStatus(
            @RequestParam List<Long> appointmentIds,
            @RequestParam RendezVousStatut newStatus,
            Authentication authentication) {
        logger.info("🔄 Bulk status update for {} appointments to status: {}", appointmentIds.size(), newStatus);

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            int successCount = 0;
            int failCount = 0;

            for (Long id : appointmentIds) {
                try {
                    rendezVousService.updateStatus(id, newStatus, requestingUserId);
                    successCount++;
                } catch (Exception e) {
                    logger.warn("Failed to update appointment {}: {}", id, e.getMessage());
                    failCount++;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "status", "completed",
                    "totalRequested", appointmentIds.size(),
                    "successCount", successCount,
                    "failCount", failCount,
                    "newStatus", newStatus.toString()
            ));
        } catch (Exception e) {
            logger.error("❌ Bulk status update failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Bulk cancel appointments")
    @DeleteMapping("/bulk/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> bulkCancelAppointments(
            @RequestParam List<Long> appointmentIds,
            Authentication authentication) {
        logger.info("❌ Bulk cancellation for {} appointments", appointmentIds.size());

        try {
            Long requestingUserId = extractUserIdFromAuth(authentication);
            int successCount = 0;
            int failCount = 0;

            for (Long id : appointmentIds) {
                try {
                    rendezVousService.cancelRendezVous(id, requestingUserId);
                    successCount++;
                } catch (Exception e) {
                    logger.warn("Failed to cancel appointment {}: {}", id, e.getMessage());
                    failCount++;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "status", "completed",
                    "totalRequested", appointmentIds.size(),
                    "successCount", successCount,
                    "failCount", failCount,
                    "operation", "cancel"
            ));
        } catch (Exception e) {
            logger.error("❌ Bulk cancellation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // REPORTING ENDPOINTS
    // ========================================

    @Operation(summary = "Generate appointment report")
    @GetMapping("/reports/appointments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> generateAppointmentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) RendezVousStatut status) {
        logger.info("📊 Generating appointment report from {} to {}", startDate, endDate);

        try {
            Map<String, Object> report = new HashMap<>();

            // Get appointments in date range
            List<RendezVousDto> appointments = rendezVousService.findByDateRange(startDate, endDate);

            // Filter by doctor if specified
            if (doctorId != null) {
                appointments = appointments.stream()
                        .filter(apt -> apt.getMedecinUserId().equals(doctorId))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Filter by status if specified
            if (status != null) {
                appointments = appointments.stream()
                        .filter(apt -> apt.getStatut().equals(status))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Generate statistics
            long totalAppointments = appointments.size();
            long confirmedCount = appointments.stream().filter(apt -> apt.getStatut() == RendezVousStatut.CONFIRME).count();
            long cancelledCount = appointments.stream().filter(apt -> apt.getStatut() == RendezVousStatut.ANNULE).count();
            long completedCount = appointments.stream().filter(apt -> apt.getStatut() == RendezVousStatut.TERMINE).count();

            report.put("reportPeriod", Map.of("start", startDate, "end", endDate));
            report.put("filters", Map.of("doctorId", doctorId, "status", status));
            report.put("summary", Map.of(
                    "totalAppointments", totalAppointments,
                    "confirmedAppointments", confirmedCount,
                    "cancelledAppointments", cancelledCount,
                    "completedAppointments", completedCount
            ));
            report.put("appointments", appointments);
            report.put("generatedAt", LocalDateTime.now());

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("❌ Report generation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ========================================
    // HEALTH & STATUS ENDPOINTS
    // ========================================

    @Operation(summary = "Health check endpoint")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Perform a simple database check
            long totalAppointments = rendezVousService.listAllRendezVous().size();

            return ResponseEntity.ok(Map.of(
                    "status", "UP",
                    "service", "RendezVousService",
                    "timestamp", System.currentTimeMillis(),
                    "version", "1.0.0",
                    "database", "CONNECTED",
                    "totalAppointments", totalAppointments
            ));
        } catch (Exception e) {
            logger.error("❌ Health check failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "status", "DOWN",
                            "service", "RendezVousService",
                            "timestamp", System.currentTimeMillis(),
                            "error", e.getMessage()
                    ));
        }
    }

    @Operation(summary = "Get service information")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getServiceInfo() {
        return ResponseEntity.ok(Map.of(
                "serviceName", "Cabinet360 Core Service - Rendez-Vous Management",
                "version", "1.0.0",
                "description", "Complete appointment management system for medical practices",
                "endpoints", Map.of(
                        "create", "POST /api/v1/rendez-vous",
                        "read", "GET /api/v1/rendez-vous/{id}",
                        "update", "PUT /api/v1/rendez-vous/{id}",
                        "delete", "DELETE /api/v1/rendez-vous/{id}",
                        "statistics", "GET /api/v1/rendez-vous/stats",
                        "availability", "GET /api/v1/rendez-vous/doctor/{id}/available-slots"
                ),
                "supportedOperations", List.of(
                        "CRUD Operations", "Conflict Detection", "Availability Checking",
                        "Statistics & Reporting", "Bulk Operations", "Maintenance"
                ),
                "timestamp", System.currentTimeMillis()
        ));
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Extract user ID from JWT authentication
     * TODO: Implement proper JWT user ID extraction based on your JWT structure
     */
    private Long extractUserIdFromAuth(Authentication authentication) {
        // This is a placeholder implementation
        // In a real application, you would extract this from the JWT token
        // For example: return ((UserPrincipal) authentication.getPrincipal()).getId();

        // For development/testing, you might want to extract from a header or use a default
        return 1L; // TODO: Replace with actual implementation
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

    /**
     * Create error response map
     */
    private Map<String, Object> createErrorResponse(String errorType, String message) {
        return Map.of(
                "error", errorType,
                "message", message,
                "timestamp", System.currentTimeMillis(),
                "path", "/api/v1/rendez-vous"
        );
    }
}
package com.cabinet360.core.service;

import com.cabinet360.core.dto.CabinetDto;
import com.cabinet360.core.dto.CabinetSettingsDto;
import com.cabinet360.core.entity.Cabinet;
import com.cabinet360.core.mapper.CabinetMapper;
import com.cabinet360.core.repository.CabinetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CabinetService {

    private static final Logger logger = LoggerFactory.getLogger(CabinetService.class);

    private final CabinetRepository cabinetRepository;

    public CabinetService(CabinetRepository cabinetRepository) {
        this.cabinetRepository = cabinetRepository;
    }

    /**
     * Create a new cabinet for a doctor
     */
    public CabinetDto createCabinet(CabinetDto cabinetDto) {
        logger.info("🏥 Creating new cabinet: {} for doctor: {}",
                cabinetDto.getCabinetName(), cabinetDto.getOwnerDoctorId());

        // Validation: Check if cabinet name already exists for this doctor
        if (cabinetRepository.existsByOwnerDoctorIdAndCabinetName(
                cabinetDto.getOwnerDoctorId(), cabinetDto.getCabinetName())) {
            throw new IllegalArgumentException(
                    "Cabinet name '" + cabinetDto.getCabinetName() + "' already exists for this doctor");
        }

        Cabinet cabinet = CabinetMapper.toEntity(cabinetDto);
        cabinet.setStatus("ACTIVE");
        cabinet.setCreatedAt(LocalDateTime.now());

        Cabinet saved = cabinetRepository.save(cabinet);
        logger.info("✅ Cabinet created successfully with ID: {}", saved.getId());

        return CabinetMapper.toDto(saved);
    }

    /**
     * Update cabinet information
     */
    public CabinetDto updateCabinet(Long cabinetId, CabinetDto cabinetDto) {
        logger.info("🔄 Updating cabinet: {}", cabinetId);

        Cabinet existing = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found with ID: " + cabinetId));

        // Update fields
        existing.setCabinetName(cabinetDto.getCabinetName());
        existing.setDescription(cabinetDto.getDescription());
        existing.setBusinessLicense(cabinetDto.getBusinessLicense());

        if (cabinetDto.getSettings() != null) {
            existing.setSettings(CabinetMapper.toSettingsEntity(cabinetDto.getSettings()));
        }

        existing.setUpdatedAt(LocalDateTime.now());

        Cabinet updated = cabinetRepository.save(existing);
        logger.info("✅ Cabinet updated successfully: {}", cabinetId);

        return CabinetMapper.toDto(updated);
    }

    /**
     * Update cabinet settings only
     */
    public CabinetDto updateCabinetSettings(Long cabinetId, CabinetSettingsDto settingsDto) {
        logger.info("⚙️ Updating cabinet settings: {}", cabinetId);

        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found with ID: " + cabinetId));

        cabinet.setSettings(CabinetMapper.toSettingsEntity(settingsDto));
        cabinet.setUpdatedAt(LocalDateTime.now());

        Cabinet updated = cabinetRepository.save(cabinet);
        logger.info("✅ Cabinet settings updated successfully: {}", cabinetId);

        return CabinetMapper.toDto(updated);
    }

    /**
     * Get cabinet by ID
     */
    @Transactional(readOnly = true)
    public Optional<CabinetDto> getCabinetById(Long cabinetId) {
        return cabinetRepository.findById(cabinetId)
                .map(CabinetMapper::toDto);
    }

    /**
     * Get all cabinets owned by a doctor
     */
    @Transactional(readOnly = true)
    public List<CabinetDto> getCabinetsByDoctor(Long doctorId) {
        return cabinetRepository.findByOwnerDoctorId(doctorId).stream()
                .map(CabinetMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active cabinets owned by a doctor
     */
    @Transactional(readOnly = true)
    public List<CabinetDto> getActiveCabinetsByDoctor(Long doctorId) {
        return cabinetRepository.findByOwnerDoctorIdAndStatusAndDeletedAtIsNull(doctorId, "ACTIVE").stream()
                .map(CabinetMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active cabinets
     */
    @Transactional(readOnly = true)
    public List<CabinetDto> getAllActiveCabinets() {
        return cabinetRepository.findByStatusAndDeletedAtIsNull("ACTIVE").stream()
                .map(CabinetMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Search cabinets by name
     */
    @Transactional(readOnly = true)
    public List<CabinetDto> searchCabinetsByName(String name) {
        return cabinetRepository.searchByName(name).stream()
                .map(CabinetMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Activate cabinet
     */
    public CabinetDto activateCabinet(Long cabinetId) {
        logger.info("🟢 Activating cabinet: {}", cabinetId);

        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found with ID: " + cabinetId));

        cabinet.activate();
        Cabinet updated = cabinetRepository.save(cabinet);

        logger.info("✅ Cabinet activated: {}", cabinetId);
        return CabinetMapper.toDto(updated);
    }

    /**
     * Deactivate cabinet
     */
    public CabinetDto deactivateCabinet(Long cabinetId) {
        logger.info("🔴 Deactivating cabinet: {}", cabinetId);

        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found with ID: " + cabinetId));

        cabinet.deactivate();
        Cabinet updated = cabinetRepository.save(cabinet);

        logger.info("✅ Cabinet deactivated: {}", cabinetId);
        return CabinetMapper.toDto(updated);
    }

    /**
     * Suspend cabinet
     */
    public CabinetDto suspendCabinet(Long cabinetId) {
        logger.info("⏸️ Suspending cabinet: {}", cabinetId);

        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found with ID: " + cabinetId));

        cabinet.suspend();
        Cabinet updated = cabinetRepository.save(cabinet);

        logger.info("✅ Cabinet suspended: {}", cabinetId);
        return CabinetMapper.toDto(updated);
    }

    /**
     * Soft delete cabinet
     */
    public void deleteCabinet(Long cabinetId) {
        logger.info("🗑️ Soft deleting cabinet: {}", cabinetId);

        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found with ID: " + cabinetId));

        cabinet.softDelete();
        cabinetRepository.save(cabinet);

        logger.info("✅ Cabinet soft deleted: {}", cabinetId);
    }

    /**
     * Check if doctor owns cabinet
     */
    @Transactional(readOnly = true)
    public boolean isDoctorOwner(Long doctorId, Long cabinetId) {
        return cabinetRepository.findById(cabinetId)
                .map(cabinet -> cabinet.getOwnerDoctorId().equals(doctorId))
                .orElse(false);
    }

    /**
     * Get cabinets with online booking enabled
     */
    @Transactional(readOnly = true)
    public List<CabinetDto> getCabinetsWithOnlineBooking() {
        return cabinetRepository.findCabinetsWithOnlineBooking().stream()
                .map(CabinetMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Count active cabinets for a doctor
     */
    @Transactional(readOnly = true)
    public long countActiveCabinetsByDoctor(Long doctorId) {
        return cabinetRepository.countActiveCabinetsByDoctor(doctorId);
    }

    /**
     * Get cabinet statistics
     */
    @Transactional(readOnly = true)
    public CabinetStatsDto getCabinetStats(Long cabinetId) {
        Cabinet cabinet = cabinetRepository.findById(cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Cabinet not found with ID: " + cabinetId));

        // This would integrate with other services to get real stats
        return CabinetStatsDto.builder()
                .cabinetId(cabinetId)
                .totalPatients(0L) // Would get from PatientCabinetLinkService
                .totalAppointments(0L) // Would get from RendezVousService
                .activeAppointments(0L) // Would get from RendezVousService
                .isActive(cabinet.isActive())
                .lastUpdated(cabinet.getUpdatedAt())
                .build();
    }

    // === Helper class for statistics ===
    public static class CabinetStatsDto {
        private Long cabinetId;
        private Long totalPatients;
        private Long totalAppointments;
        private Long activeAppointments;
        private boolean isActive;
        private LocalDateTime lastUpdated;

        // Builder pattern, constructors, getters/setters...
        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private Long cabinetId;
            private Long totalPatients;
            private Long totalAppointments;
            private Long activeAppointments;
            private boolean isActive;
            private LocalDateTime lastUpdated;

            public Builder cabinetId(Long cabinetId) { this.cabinetId = cabinetId; return this; }
            public Builder totalPatients(Long total) { this.totalPatients = total; return this; }
            public Builder totalAppointments(Long total) { this.totalAppointments = total; return this; }
            public Builder activeAppointments(Long active) { this.activeAppointments = active; return this; }
            public Builder isActive(boolean active) { this.isActive = active; return this; }
            public Builder lastUpdated(LocalDateTime updated) { this.lastUpdated = updated; return this; }

            public CabinetStatsDto build() {
                CabinetStatsDto stats = new CabinetStatsDto();
                stats.cabinetId = this.cabinetId;
                stats.totalPatients = this.totalPatients;
                stats.totalAppointments = this.totalAppointments;
                stats.activeAppointments = this.activeAppointments;
                stats.isActive = this.isActive;
                stats.lastUpdated = this.lastUpdated;
                return stats;
            }
        }

        // Getters
        public Long getCabinetId() { return cabinetId; }
        public Long getTotalPatients() { return totalPatients; }
        public Long getTotalAppointments() { return totalAppointments; }
        public Long getActiveAppointments() { return activeAppointments; }
        public boolean isActive() { return isActive; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
    }

    @Override
    public String toString() {
        return "CabinetService{" +
                "repository=" + cabinetRepository.getClass().getSimpleName() +
                '}';
    }
}
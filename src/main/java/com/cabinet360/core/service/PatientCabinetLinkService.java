package com.cabinet360.core.service;

import com.cabinet360.core.dto.PatientCabinetLinkDto;
import com.cabinet360.core.entity.PatientCabinetLink;
import com.cabinet360.core.mapper.PatientCabinetLinkMapper;
import com.cabinet360.core.repository.PatientCabinetLinkRepository;
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
public class PatientCabinetLinkService {

    private static final Logger logger = LoggerFactory.getLogger(PatientCabinetLinkService.class);

    private final PatientCabinetLinkRepository repository;

    public PatientCabinetLinkService(PatientCabinetLinkRepository repository) {
        this.repository = repository;
    }

    /**
     * Create a link between patient and cabinet
     */
    public PatientCabinetLinkDto linkPatientToCabinet(Long patientUserId, Long cabinetId) {
        logger.info("🔗 Linking patient {} to cabinet {}", patientUserId, cabinetId);

        // Check if link already exists
        if (repository.existsByPatientUserIdAndCabinetId(patientUserId, cabinetId)) {
            throw new IllegalArgumentException("Patient is already linked to this cabinet");
        }

        PatientCabinetLink link = new PatientCabinetLink(patientUserId, cabinetId);
        PatientCabinetLink saved = repository.save(link);

        logger.info("✅ Patient-Cabinet link created: {}", saved.getId());
        return PatientCabinetLinkMapper.toDto(saved);
    }

    /**
     * Create a link with custom parameters
     */
    public PatientCabinetLinkDto createLink(PatientCabinetLinkDto linkDto) {
        logger.info("🔗 Creating custom link: patient {} to cabinet {}",
                linkDto.getPatientUserId(), linkDto.getCabinetId());

        // Check if link already exists
        if (repository.existsByPatientUserIdAndCabinetId(
                linkDto.getPatientUserId(), linkDto.getCabinetId())) {
            throw new IllegalArgumentException("Patient is already linked to this cabinet");
        }

        PatientCabinetLink link = PatientCabinetLinkMapper.toEntity(linkDto);
        if (link.getLinkedAt() == null) {
            link.setLinkedAt(LocalDateTime.now());
        }
        if (link.getStatus() == null) {
            link.setStatus("ACTIVE");
        }

        PatientCabinetLink saved = repository.save(link);
        logger.info("✅ Custom link created: {}", saved.getId());
        return PatientCabinetLinkMapper.toDto(saved);
    }

    /**
     * Update existing link
     */
    public PatientCabinetLinkDto updateLink(Long linkId, PatientCabinetLinkDto linkDto) {
        logger.info("🔄 Updating link: {}", linkId);

        PatientCabinetLink existing = repository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with ID: " + linkId));

        // Update fields
        existing.setStatus(linkDto.getStatus());
        existing.setLinkNotes(linkDto.getLinkNotes());
        existing.setLastAccessAt(linkDto.getLastAccessAt());

        PatientCabinetLink updated = repository.save(existing);
        logger.info("✅ Link updated: {}", linkId);
        return PatientCabinetLinkMapper.toDto(updated);
    }

    /**
     * Get link by ID
     */
    @Transactional(readOnly = true)
    public Optional<PatientCabinetLinkDto> getLinkById(Long linkId) {
        return repository.findById(linkId)
                .map(PatientCabinetLinkMapper::toDto);
    }

    /**
     * Get specific link between patient and cabinet
     */
    @Transactional(readOnly = true)
    public Optional<PatientCabinetLinkDto> getLink(Long patientUserId, Long cabinetId) {
        return repository.findByPatientUserIdAndCabinetId(patientUserId, cabinetId)
                .map(PatientCabinetLinkMapper::toDto);
    }

    /**
     * Get all links for a patient
     */
    @Transactional(readOnly = true)
    public List<PatientCabinetLinkDto> getLinksByPatient(Long patientUserId) {
        return repository.findByPatientUserId(patientUserId).stream()
                .map(PatientCabinetLinkMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active links for a patient
     */
    @Transactional(readOnly = true)
    public List<PatientCabinetLinkDto> getActiveLinksByPatient(Long patientUserId) {
        return repository.findByPatientUserIdAndStatusAndDeletedAtIsNull(patientUserId, "ACTIVE").stream()
                .map(PatientCabinetLinkMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all links for a cabinet
     */
    @Transactional(readOnly = true)
    public List<PatientCabinetLinkDto> getLinksByCabinet(Long cabinetId) {
        return repository.findByCabinetId(cabinetId).stream()
                .map(PatientCabinetLinkMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get active links for a cabinet
     */
    @Transactional(readOnly = true)
    public List<PatientCabinetLinkDto> getActiveLinksByCabinet(Long cabinetId) {
        return repository.findByCabinetIdAndStatusAndDeletedAtIsNull(cabinetId, "ACTIVE").stream()
                .map(PatientCabinetLinkMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get pending links
     */
    @Transactional(readOnly = true)
    public List<PatientCabinetLinkDto> getPendingLinks() {
        return repository.findByStatusAndDeletedAtIsNull("PENDING").stream()
                .map(PatientCabinetLinkMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Activate link
     */
    public PatientCabinetLinkDto activateLink(Long linkId) {
        logger.info("🟢 Activating link: {}", linkId);

        PatientCabinetLink link = repository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with ID: " + linkId));

        link.activate();
        PatientCabinetLink updated = repository.save(link);

        logger.info("✅ Link activated: {}", linkId);
        return PatientCabinetLinkMapper.toDto(updated);
    }

    /**
     * Deactivate link
     */
    public PatientCabinetLinkDto deactivateLink(Long linkId) {
        logger.info("🔴 Deactivating link: {}", linkId);

        PatientCabinetLink link = repository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with ID: " + linkId));

        link.deactivate();
        PatientCabinetLink updated = repository.save(link);

        logger.info("✅ Link deactivated: {}", linkId);
        return PatientCabinetLinkMapper.toDto(updated);
    }

    /**
     * Revoke link
     */
    public PatientCabinetLinkDto revokeLink(Long linkId) {
        logger.info("❌ Revoking link: {}", linkId);

        PatientCabinetLink link = repository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with ID: " + linkId));

        link.revoke();
        PatientCabinetLink updated = repository.save(link);

        logger.info("✅ Link revoked: {}", linkId);
        return PatientCabinetLinkMapper.toDto(updated);
    }

    /**
     * Soft delete link
     */
    public void deleteLink(Long linkId) {
        logger.info("🗑️ Soft deleting link: {}", linkId);

        PatientCabinetLink link = repository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Link not found with ID: " + linkId));

        link.softDelete();
        repository.save(link);

        logger.info("✅ Link soft deleted: {}", linkId);
    }

    /**
     * Hard delete link between patient and cabinet
     */
    public void unlinkPatientFromCabinet(Long patientUserId, Long cabinetId) {
        logger.info("🗑️ Unlinking patient {} from cabinet {}", patientUserId, cabinetId);
        repository.deleteByPatientUserIdAndCabinetId(patientUserId, cabinetId);
        logger.info("✅ Patient unlinked from cabinet");
    }

    /**
     * Update last access time
     */
    public PatientCabinetLinkDto updateLastAccess(Long patientUserId, Long cabinetId) {
        PatientCabinetLink link = repository.findByPatientUserIdAndCabinetIdAndStatusAndDeletedAtIsNull(
                        patientUserId, cabinetId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("Active link not found"));

        link.updateLastAccess();
        PatientCabinetLink updated = repository.save(link);
        return PatientCabinetLinkMapper.toDto(updated);
    }

    /**
     * Count active links for patient
     */
    @Transactional(readOnly = true)
    public long countActiveLinksForPatient(Long patientUserId) {
        return repository.countActiveLinksForPatient(patientUserId);
    }

    /**
     * Count active links for cabinet
     */
    @Transactional(readOnly = true)
    public long countActiveLinksForCabinet(Long cabinetId) {
        return repository.countActiveLinksForCabinet(cabinetId);
    }

    /**
     * Check if patient is linked to cabinet
     */
    @Transactional(readOnly = true)
    public boolean isPatientLinkedToCabinet(Long patientUserId, Long cabinetId) {
        return repository.findByPatientUserIdAndCabinetIdAndStatusAndDeletedAtIsNull(
                patientUserId, cabinetId, "ACTIVE").isPresent();
    }
}
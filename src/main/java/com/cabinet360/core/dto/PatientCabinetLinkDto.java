package com.cabinet360.core.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

public class PatientCabinetLinkDto {

    private Long id;

    @NotNull(message = "Patient user ID is required")
    private Long patientUserId;

    @NotNull(message = "Cabinet ID is required")
    private Long cabinetId;

    private String status;
    private LocalDateTime linkedAt;
    private LocalDateTime lastAccessAt;
    private String linkNotes;
    private LocalDateTime deletedAt;

    // === Constructors ===

    public PatientCabinetLinkDto() {}

    public PatientCabinetLinkDto(Long id, Long patientUserId, Long cabinetId, String status,
                                 LocalDateTime linkedAt, LocalDateTime lastAccessAt,
                                 String linkNotes, LocalDateTime deletedAt) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.cabinetId = cabinetId;
        this.status = status;
        this.linkedAt = linkedAt;
        this.lastAccessAt = lastAccessAt;
        this.linkNotes = linkNotes;
        this.deletedAt = deletedAt;
    }

    // === Builder Pattern ===
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long patientUserId;
        private Long cabinetId;
        private String status = "ACTIVE";
        private LocalDateTime linkedAt;
        private LocalDateTime lastAccessAt;
        private String linkNotes;
        private LocalDateTime deletedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder patientUserId(Long patientUserId) { this.patientUserId = patientUserId; return this; }
        public Builder cabinetId(Long cabinetId) { this.cabinetId = cabinetId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder linkedAt(LocalDateTime linkedAt) { this.linkedAt = linkedAt; return this; }
        public Builder lastAccessAt(LocalDateTime lastAccessAt) { this.lastAccessAt = lastAccessAt; return this; }
        public Builder linkNotes(String linkNotes) { this.linkNotes = linkNotes; return this; }
        public Builder deletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; return this; }

        public PatientCabinetLinkDto build() {
            return new PatientCabinetLinkDto(id, patientUserId, cabinetId, status,
                    linkedAt, lastAccessAt, linkNotes, deletedAt);
        }
    }

    // === Business Methods ===

    public boolean isActive() {
        return "ACTIVE".equals(status) && deletedAt == null;
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isRevoked() {
        return "REVOKED".equals(status);
    }

    // === Getters and Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    public Long getCabinetId() { return cabinetId; }
    public void setCabinetId(Long cabinetId) { this.cabinetId = cabinetId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLinkedAt() { return linkedAt; }
    public void setLinkedAt(LocalDateTime linkedAt) { this.linkedAt = linkedAt; }

    public LocalDateTime getLastAccessAt() { return lastAccessAt; }
    public void setLastAccessAt(LocalDateTime lastAccessAt) { this.lastAccessAt = lastAccessAt; }

    public String getLinkNotes() { return linkNotes; }
    public void setLinkNotes(String linkNotes) { this.linkNotes = linkNotes; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientCabinetLinkDto that = (PatientCabinetLinkDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(patientUserId, that.patientUserId) &&
                Objects.equals(cabinetId, that.cabinetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, patientUserId, cabinetId);
    }

    @Override
    public String toString() {
        return "PatientCabinetLinkDto{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", cabinetId=" + cabinetId +
                ", status='" + status + '\'' +
                ", linkedAt=" + linkedAt +
                '}';
    }
}
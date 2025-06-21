package com.cabinet360.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "patient_cabinet_links",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_patient_cabinet", columnNames = {"patient_user_id", "cabinet_id"})
        },
        indexes = {
                @Index(name = "idx_patient_cabinet_patient", columnList = "patient_user_id"),
                @Index(name = "idx_patient_cabinet_cabinet", columnList = "cabinet_id"),
                @Index(name = "idx_patient_cabinet_status", columnList = "status")
        }
)
public class PatientCabinetLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to auth-service PatientUser.id
     */
    @NotNull(message = "Patient user ID is required")
    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    /**
     * Reference to Cabinet.id
     */
    @NotNull(message = "Cabinet ID is required")
    @Column(name = "cabinet_id", nullable = false)
    private Long cabinetId;

    /**
     * Link status: ACTIVE, INACTIVE, PENDING, REVOKED
     */
    @NotNull
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    /**
     * When the link was created
     */
    @Column(name = "linked_at", nullable = false, updatable = false)
    private LocalDateTime linkedAt = LocalDateTime.now();

    /**
     * Last time patient accessed this cabinet
     */
    @Column(name = "last_access_at")
    private LocalDateTime lastAccessAt;

    /**
     * Optional notes about the relationship
     */
    @Column(name = "link_notes", length = 500)
    private String linkNotes;

    /**
     * Soft delete timestamp
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // === Constructors ===

    public PatientCabinetLink() {}

    public PatientCabinetLink(Long patientUserId, Long cabinetId) {
        this.patientUserId = patientUserId;
        this.cabinetId = cabinetId;
        this.status = "ACTIVE";
        this.linkedAt = LocalDateTime.now();
    }

    // === Business Methods ===

    public boolean isActive() {
        return "ACTIVE".equals(status) && deletedAt == null;
    }

    public void activate() {
        this.status = "ACTIVE";
        this.deletedAt = null;
    }

    public void deactivate() {
        this.status = "INACTIVE";
    }

    public void revoke() {
        this.status = "REVOKED";
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = "INACTIVE";
    }

    public void updateLastAccess() {
        this.lastAccessAt = LocalDateTime.now();
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
        PatientCabinetLink that = (PatientCabinetLink) o;
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
        return "PatientCabinetLink{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", cabinetId=" + cabinetId +
                ", status='" + status + '\'' +
                ", linkedAt=" + linkedAt +
                '}';
    }
}
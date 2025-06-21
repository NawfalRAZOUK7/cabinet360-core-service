package com.cabinet360.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "cabinets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cabinet_owner", columnNames = {"owner_doctor_id", "cabinet_name"})
        },
        indexes = {
                @Index(name = "idx_cabinet_owner", columnList = "owner_doctor_id"),
                @Index(name = "idx_cabinet_status", columnList = "status"),
                @Index(name = "idx_cabinet_created", columnList = "created_at")
        }
)
public class Cabinet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to auth-service DoctorUser.id (cabinet owner)
     */
    @NotNull(message = "Owner doctor ID is required")
    @Column(name = "owner_doctor_id", nullable = false)
    private Long ownerDoctorId;

    /**
     * Business name of the medical practice
     */
    @NotNull(message = "Cabinet name is required")
    @Size(min = 2, max = 200, message = "Cabinet name must be between 2 and 200 characters")
    @Column(name = "cabinet_name", nullable = false, length = 200)
    private String cabinetName;

    /**
     * Business description
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    /**
     * Medical business license number
     */
    @Size(max = 100, message = "License number must not exceed 100 characters")
    @Column(name = "business_license", length = 100)
    private String businessLicense;

    /**
     * Cabinet status: ACTIVE, INACTIVE, SUSPENDED
     */
    @NotNull
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    /**
     * Practice settings (embedded)
     */
    @Embedded
    private CabinetSettings settings;

    /**
     * Creation timestamp
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Last update timestamp
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Soft delete timestamp
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // === Constructors ===

    public Cabinet() {}

    public Cabinet(Long ownerDoctorId, String cabinetName, String description) {
        this.ownerDoctorId = ownerDoctorId;
        this.cabinetName = cabinetName;
        this.description = description;
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.settings = new CabinetSettings();
    }

    // === Builder Pattern ===
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long ownerDoctorId;
        private String cabinetName;
        private String description;
        private String businessLicense;
        private String status = "ACTIVE";
        private CabinetSettings settings;

        public Builder ownerDoctorId(Long ownerDoctorId) {
            this.ownerDoctorId = ownerDoctorId;
            return this;
        }

        public Builder cabinetName(String cabinetName) {
            this.cabinetName = cabinetName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder businessLicense(String businessLicense) {
            this.businessLicense = businessLicense;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder settings(CabinetSettings settings) {
            this.settings = settings;
            return this;
        }

        public Cabinet build() {
            Cabinet cabinet = new Cabinet();
            cabinet.ownerDoctorId = this.ownerDoctorId;
            cabinet.cabinetName = this.cabinetName;
            cabinet.description = this.description;
            cabinet.businessLicense = this.businessLicense;
            cabinet.status = this.status;
            cabinet.settings = this.settings != null ? this.settings : new CabinetSettings();
            cabinet.createdAt = LocalDateTime.now();
            return cabinet;
        }
    }

    // === Business Methods ===

    public boolean isActive() {
        return "ACTIVE".equals(status) && deletedAt == null;
    }

    public void activate() {
        this.status = "ACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = "SUSPENDED";
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = "INACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === Getters and Setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerDoctorId() { return ownerDoctorId; }
    public void setOwnerDoctorId(Long ownerDoctorId) { this.ownerDoctorId = ownerDoctorId; }

    public String getCabinetName() { return cabinetName; }
    public void setCabinetName(String cabinetName) { this.cabinetName = cabinetName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBusinessLicense() { return businessLicense; }
    public void setBusinessLicense(String businessLicense) { this.businessLicense = businessLicense; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public CabinetSettings getSettings() { return settings; }
    public void setSettings(CabinetSettings settings) { this.settings = settings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // === equals, hashCode, toString ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cabinet cabinet = (Cabinet) o;
        return Objects.equals(id, cabinet.id) &&
               Objects.equals(ownerDoctorId, cabinet.ownerDoctorId) &&
               Objects.equals(cabinetName, cabinet.cabinetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerDoctorId, cabinetName);
    }

    @Override
    public String toString() {
        return "Cabinet{" +
                "id=" + id +
                ", ownerDoctorId=" + ownerDoctorId +
                ", cabinetName='" + cabinetName + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
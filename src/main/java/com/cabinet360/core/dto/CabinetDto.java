package com.cabinet360.core.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

public class CabinetDto {

    private Long id;

    @NotNull(message = "Owner doctor ID is required")
    private Long ownerDoctorId;

    @NotNull(message = "Cabinet name is required")
    @Size(min = 2, max = 200, message = "Cabinet name must be between 2 and 200 characters")
    private String cabinetName;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String businessLicense;

    private String status;

    private CabinetSettingsDto settings;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // === Constructors ===

    public CabinetDto() {}

    public CabinetDto(Long id, Long ownerDoctorId, String cabinetName, String description,
                      String businessLicense, String status, CabinetSettingsDto settings,
                      LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt) {
        this.id = id;
        this.ownerDoctorId = ownerDoctorId;
        this.cabinetName = cabinetName;
        this.description = description;
        this.businessLicense = businessLicense;
        this.status = status;
        this.settings = settings;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    // === Builder Pattern ===
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long ownerDoctorId;
        private String cabinetName;
        private String description;
        private String businessLicense;
        private String status;
        private CabinetSettingsDto settings;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

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

        public Builder settings(CabinetSettingsDto settings) {
            this.settings = settings;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder deletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public CabinetDto build() {
            return new CabinetDto(id, ownerDoctorId, cabinetName, description, businessLicense,
                    status, settings, createdAt, updatedAt, deletedAt);
        }
    }

    // === Business Methods ===

    public boolean isActive() {
        return "ACTIVE".equals(status) && deletedAt == null;
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

    public CabinetSettingsDto getSettings() { return settings; }
    public void setSettings(CabinetSettingsDto settings) { this.settings = settings; }

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
        CabinetDto that = (CabinetDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(ownerDoctorId, that.ownerDoctorId) &&
                Objects.equals(cabinetName, that.cabinetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerDoctorId, cabinetName);
    }

    @Override
    public String toString() {
        return "CabinetDto{" +
                "id=" + id +
                ", ownerDoctorId=" + ownerDoctorId +
                ", cabinetName='" + cabinetName + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
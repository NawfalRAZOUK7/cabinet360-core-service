package com.cabinet360.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime; // ✅ Fixed: Changed from LocalDate to LocalDateTime
import java.util.Objects;

/**
 * DTO representing a medical prescription (Ordonnance).
 * ✅ Fixed field mappings to match entity.
 */
public class OrdonnanceDto {

    private Long id;

    @NotBlank(message = "Le contenu de l'ordonnance est obligatoire")
    @Size(max = 1500)
    private String contenu; // ✅ Fixed: Changed from 'description' to 'contenu'

    @NotNull(message = "La date de l'ordonnance est obligatoire")
    private LocalDateTime dateOrdonnance; // ✅ Fixed: Changed from LocalDate to LocalDateTime

    @NotNull(message = "L'identifiant du médecin est obligatoire")
    private Long medecinUserId; // ✅ Added: Missing field from entity

    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientUserId; // ✅ Added: Missing field from entity

    @NotNull(message = "L'identifiant du dossier médical est obligatoire")
    private Long dossierMedicalId;

    // --- Constructors ---
    public OrdonnanceDto() {}

    public OrdonnanceDto(Long id, String contenu, LocalDateTime dateOrdonnance,
                         Long medecinUserId, Long patientUserId, Long dossierMedicalId) {
        this.id = id;
        this.contenu = contenu;
        this.dateOrdonnance = dateOrdonnance;
        this.medecinUserId = medecinUserId;
        this.patientUserId = patientUserId;
        this.dossierMedicalId = dossierMedicalId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateOrdonnance() { return dateOrdonnance; }
    public void setDateOrdonnance(LocalDateTime dateOrdonnance) { this.dateOrdonnance = dateOrdonnance; }

    public Long getMedecinUserId() { return medecinUserId; }
    public void setMedecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    public Long getDossierMedicalId() { return dossierMedicalId; }
    public void setDossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    // --- equals and hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrdonnanceDto)) return false;
        OrdonnanceDto that = (OrdonnanceDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "OrdonnanceDto{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateOrdonnance=" + dateOrdonnance +
                ", medecinUserId=" + medecinUserId +
                ", patientUserId=" + patientUserId +
                ", dossierMedicalId=" + dossierMedicalId +
                '}';
    }

    // --- Builder pattern ---
    public static class Builder {
        private Long id;
        private String contenu;
        private LocalDateTime dateOrdonnance;
        private Long medecinUserId;
        private Long patientUserId;
        private Long dossierMedicalId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder contenu(String contenu) { this.contenu = contenu; return this; }
        public Builder dateOrdonnance(LocalDateTime dateOrdonnance) { this.dateOrdonnance = dateOrdonnance; return this; }
        public Builder medecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; return this; }
        public Builder patientUserId(Long patientUserId) { this.patientUserId = patientUserId; return this; }
        public Builder dossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; return this; }

        public OrdonnanceDto build() {
            return new OrdonnanceDto(id, contenu, dateOrdonnance, medecinUserId, patientUserId, dossierMedicalId);
        }
    }
}
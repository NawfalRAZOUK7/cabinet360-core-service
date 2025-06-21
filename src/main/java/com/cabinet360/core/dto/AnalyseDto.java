package com.cabinet360.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime; // ✅ Fixed: Changed from LocalDate to LocalDateTime
import java.util.Objects;

/**
 * Data Transfer Object for Analyse.
 * ✅ Fixed field mappings to match entity.
 */
public class AnalyseDto {

    private Long id;

    @NotBlank(message = "Le type de l'analyse est obligatoire")
    @Size(max = 255)
    private String typeAnalyse; // ✅ Fixed: Changed from 'nom' to 'typeAnalyse'

    @NotBlank(message = "Le résultat de l'analyse est obligatoire")
    @Size(max = 1000)
    private String resultat;

    @NotNull(message = "La date de l'analyse est obligatoire")
    private LocalDateTime dateAnalyse; // ✅ Fixed: Changed from LocalDate to LocalDateTime

    @NotNull(message = "L'identifiant du dossier médical est obligatoire")
    private Long dossierMedicalId;

    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientUserId; // ✅ Added: Missing field from entity

    // --- Constructors ---
    public AnalyseDto() {}

    public AnalyseDto(Long id, String typeAnalyse, String resultat, LocalDateTime dateAnalyse,
                      Long dossierMedicalId, Long patientUserId) {
        this.id = id;
        this.typeAnalyse = typeAnalyse;
        this.resultat = resultat;
        this.dateAnalyse = dateAnalyse;
        this.dossierMedicalId = dossierMedicalId;
        this.patientUserId = patientUserId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeAnalyse() { return typeAnalyse; }
    public void setTypeAnalyse(String typeAnalyse) { this.typeAnalyse = typeAnalyse; }

    public String getResultat() { return resultat; }
    public void setResultat(String resultat) { this.resultat = resultat; }

    public LocalDateTime getDateAnalyse() { return dateAnalyse; }
    public void setDateAnalyse(LocalDateTime dateAnalyse) { this.dateAnalyse = dateAnalyse; }

    public Long getDossierMedicalId() { return dossierMedicalId; }
    public void setDossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    // --- equals and hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnalyseDto)) return false;
        AnalyseDto that = (AnalyseDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "AnalyseDto{" +
                "id=" + id +
                ", typeAnalyse='" + typeAnalyse + '\'' +
                ", resultat='" + resultat + '\'' +
                ", dateAnalyse=" + dateAnalyse +
                ", dossierMedicalId=" + dossierMedicalId +
                ", patientUserId=" + patientUserId +
                '}';
    }

    // --- Builder pattern ---
    public static class Builder {
        private Long id;
        private String typeAnalyse;
        private String resultat;
        private LocalDateTime dateAnalyse;
        private Long dossierMedicalId;
        private Long patientUserId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder typeAnalyse(String typeAnalyse) { this.typeAnalyse = typeAnalyse; return this; }
        public Builder resultat(String resultat) { this.resultat = resultat; return this; }
        public Builder dateAnalyse(LocalDateTime dateAnalyse) { this.dateAnalyse = dateAnalyse; return this; }
        public Builder dossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; return this; }
        public Builder patientUserId(Long patientUserId) { this.patientUserId = patientUserId; return this; }

        public AnalyseDto build() {
            return new AnalyseDto(id, typeAnalyse, resultat, dateAnalyse, dossierMedicalId, patientUserId);
        }
    }
}
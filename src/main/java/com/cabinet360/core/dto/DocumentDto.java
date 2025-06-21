package com.cabinet360.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime; // ✅ Fixed: Changed from LocalDate to LocalDateTime
import java.util.Objects;

/**
 * Data Transfer Object for Document.
 * ✅ Fixed field mappings to match entity.
 */
public class DocumentDto {

    private Long id;

    @NotBlank(message = "Le nom du document est obligatoire")
    @Size(max = 255)
    private String nom;

    @NotBlank(message = "L'URL du fichier est obligatoire")
    @Size(max = 500)
    private String url; // ✅ Fixed: Changed from 'urlFichier' to 'url'

    @Size(max = 100)
    private String typeDocument; // ✅ Added: Missing field from entity

    @NotNull(message = "La date d'upload est obligatoire")
    private LocalDateTime dateUpload; // ✅ Fixed: Changed from LocalDate to LocalDateTime

    @NotNull(message = "L'identifiant du dossier médical est obligatoire")
    private Long dossierMedicalId;

    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientUserId; // ✅ Added: Missing field from entity

    // --- Constructors ---
    public DocumentDto() {}

    public DocumentDto(Long id, String nom, String url, String typeDocument,
                       LocalDateTime dateUpload, Long dossierMedicalId, Long patientUserId) {
        this.id = id;
        this.nom = nom;
        this.url = url;
        this.typeDocument = typeDocument;
        this.dateUpload = dateUpload;
        this.dossierMedicalId = dossierMedicalId;
        this.patientUserId = patientUserId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }

    public LocalDateTime getDateUpload() { return dateUpload; }
    public void setDateUpload(LocalDateTime dateUpload) { this.dateUpload = dateUpload; }

    public Long getDossierMedicalId() { return dossierMedicalId; }
    public void setDossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    // --- equals and hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentDto)) return false;
        DocumentDto that = (DocumentDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DocumentDto{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", url='" + url + '\'' +
                ", typeDocument='" + typeDocument + '\'' +
                ", dateUpload=" + dateUpload +
                ", dossierMedicalId=" + dossierMedicalId +
                ", patientUserId=" + patientUserId +
                '}';
    }

    // --- Builder pattern ---
    public static class Builder {
        private Long id;
        private String nom;
        private String url;
        private String typeDocument;
        private LocalDateTime dateUpload;
        private Long dossierMedicalId;
        private Long patientUserId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nom(String nom) { this.nom = nom; return this; }
        public Builder url(String url) { this.url = url; return this; }
        public Builder typeDocument(String typeDocument) { this.typeDocument = typeDocument; return this; }
        public Builder dateUpload(LocalDateTime dateUpload) { this.dateUpload = dateUpload; return this; }
        public Builder dossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; return this; }
        public Builder patientUserId(Long patientUserId) { this.patientUserId = patientUserId; return this; }

        public DocumentDto build() {
            return new DocumentDto(id, nom, url, typeDocument, dateUpload, dossierMedicalId, patientUserId);
        }
    }
}
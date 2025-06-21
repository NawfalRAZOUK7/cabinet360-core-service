package com.cabinet360.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data Transfer Object for medical notes (NoteMedicale).
 * ✅ Fields already match entity correctly.
 */
public class NoteMedicaleDto {

    private Long id;

    @NotBlank(message = "Le contenu de la note est obligatoire")
    @Size(max = 1000, message = "Le contenu ne peut pas dépasser 1000 caractères")
    private String contenu;

    @NotNull(message = "La date de la note est obligatoire")
    private LocalDateTime dateNote;

    @NotNull(message = "L'identifiant du médecin est obligatoire")
    private Long medecinUserId; // ✅ Added validation annotation

    @NotNull(message = "L'identifiant du dossier médical est obligatoire")
    private Long dossierMedicalId;

    // --- Constructors ---
    public NoteMedicaleDto() {}

    public NoteMedicaleDto(Long id, String contenu, LocalDateTime dateNote,
                           Long medecinUserId, Long dossierMedicalId) {
        this.id = id;
        this.contenu = contenu;
        this.dateNote = dateNote;
        this.medecinUserId = medecinUserId;
        this.dossierMedicalId = dossierMedicalId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDateNote() { return dateNote; }
    public void setDateNote(LocalDateTime dateNote) { this.dateNote = dateNote; }

    public Long getMedecinUserId() { return medecinUserId; }
    public void setMedecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; }

    public Long getDossierMedicalId() { return dossierMedicalId; }
    public void setDossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; }

    // --- equals and hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteMedicaleDto)) return false;
        NoteMedicaleDto that = (NoteMedicaleDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "NoteMedicaleDto{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateNote=" + dateNote +
                ", medecinUserId=" + medecinUserId +
                ", dossierMedicalId=" + dossierMedicalId +
                '}';
    }

    // --- Builder pattern ---
    public static class Builder {
        private Long id;
        private String contenu;
        private LocalDateTime dateNote;
        private Long medecinUserId;
        private Long dossierMedicalId;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder contenu(String contenu) { this.contenu = contenu; return this; }
        public Builder dateNote(LocalDateTime dateNote) { this.dateNote = dateNote; return this; }
        public Builder medecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; return this; }
        public Builder dossierMedicalId(Long dossierMedicalId) { this.dossierMedicalId = dossierMedicalId; return this; }

        public NoteMedicaleDto build() {
            return new NoteMedicaleDto(id, contenu, dateNote, medecinUserId, dossierMedicalId);
        }
    }
}
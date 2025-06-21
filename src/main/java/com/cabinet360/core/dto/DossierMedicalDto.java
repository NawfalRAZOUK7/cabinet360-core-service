package com.cabinet360.core.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * DTO principal pour le dossier médical.
 * ✅ Fixed: Aligned with entity structure and simplified complex fields
 */
public class DossierMedicalDto {

    private Long id;

    @NotNull(message = "L'identifiant du patient est obligatoire")
    private Long patientUserId;

    // Médecin principal assigné au dossier
    private Long medecinUserId;

    // Date de création du dossier
    private LocalDateTime createdAt;

    // ✅ Added: Missing fields from entity
    private LocalDateTime updatedAt;

    // Listes d'IDs des sous-ressources (relationships)
    private List<Long> analyseIds;
    private List<Long> ordonnanceIds;
    private List<Long> documentIds;
    private List<Long> noteIds;

    // ✅ Simplified: Médecins autorisés (from complex relationship)
    private List<Long> medecinsAutorisesIds;

    // ✅ Added: Missing fields that should be in entity
    private String resumeHistorique;
    private String statut; // "ACTIF", "ARCHIVE", "SUSPENDU"
    private String commentaires;

    // --- Audit fields ---
    private Long lastAccessedBy;
    private LocalDateTime lastAccessedAt;

    // --- Constructeurs ---
    public DossierMedicalDto() {}

    public DossierMedicalDto(Long id, Long patientUserId, Long medecinUserId,
                             LocalDateTime createdAt, LocalDateTime updatedAt,
                             List<Long> analyseIds, List<Long> ordonnanceIds,
                             List<Long> documentIds, List<Long> noteIds,
                             List<Long> medecinsAutorisesIds, String resumeHistorique,
                             String statut, String commentaires, Long lastAccessedBy,
                             LocalDateTime lastAccessedAt) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.medecinUserId = medecinUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.analyseIds = analyseIds;
        this.ordonnanceIds = ordonnanceIds;
        this.documentIds = documentIds;
        this.noteIds = noteIds;
        this.medecinsAutorisesIds = medecinsAutorisesIds;
        this.resumeHistorique = resumeHistorique;
        this.statut = statut;
        this.commentaires = commentaires;
        this.lastAccessedBy = lastAccessedBy;
        this.lastAccessedAt = lastAccessedAt;
    }

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    public Long getMedecinUserId() { return medecinUserId; }
    public void setMedecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Long> getAnalyseIds() { return analyseIds; }
    public void setAnalyseIds(List<Long> analyseIds) { this.analyseIds = analyseIds; }

    public List<Long> getOrdonnanceIds() { return ordonnanceIds; }
    public void setOrdonnanceIds(List<Long> ordonnanceIds) { this.ordonnanceIds = ordonnanceIds; }

    public List<Long> getDocumentIds() { return documentIds; }
    public void setDocumentIds(List<Long> documentIds) { this.documentIds = documentIds; }

    public List<Long> getNoteIds() { return noteIds; }
    public void setNoteIds(List<Long> noteIds) { this.noteIds = noteIds; }

    public List<Long> getMedecinsAutorisesIds() { return medecinsAutorisesIds; }
    public void setMedecinsAutorisesIds(List<Long> medecinsAutorisesIds) { this.medecinsAutorisesIds = medecinsAutorisesIds; }

    public String getResumeHistorique() { return resumeHistorique; }
    public void setResumeHistorique(String resumeHistorique) { this.resumeHistorique = resumeHistorique; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }

    public Long getLastAccessedBy() { return lastAccessedBy; }
    public void setLastAccessedBy(Long lastAccessedBy) { this.lastAccessedBy = lastAccessedBy; }

    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }

    // --- equals / hashCode ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DossierMedicalDto)) return false;
        DossierMedicalDto that = (DossierMedicalDto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "DossierMedicalDto{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", medecinUserId=" + medecinUserId +
                ", createdAt=" + createdAt +
                ", statut='" + statut + '\'' +
                '}';
    }

    // --- Builder pattern ---
    public static class Builder {
        private Long id;
        private Long patientUserId;
        private Long medecinUserId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<Long> analyseIds;
        private List<Long> ordonnanceIds;
        private List<Long> documentIds;
        private List<Long> noteIds;
        private List<Long> medecinsAutorisesIds;
        private String resumeHistorique;
        private String statut;
        private String commentaires;
        private Long lastAccessedBy;
        private LocalDateTime lastAccessedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder patientUserId(Long patientUserId) { this.patientUserId = patientUserId; return this; }
        public Builder medecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder analyseIds(List<Long> analyseIds) { this.analyseIds = analyseIds; return this; }
        public Builder ordonnanceIds(List<Long> ordonnanceIds) { this.ordonnanceIds = ordonnanceIds; return this; }
        public Builder documentIds(List<Long> documentIds) { this.documentIds = documentIds; return this; }
        public Builder noteIds(List<Long> noteIds) { this.noteIds = noteIds; return this; }
        public Builder medecinsAutorisesIds(List<Long> medecinsAutorisesIds) { this.medecinsAutorisesIds = medecinsAutorisesIds; return this; }
        public Builder resumeHistorique(String resumeHistorique) { this.resumeHistorique = resumeHistorique; return this; }
        public Builder statut(String statut) { this.statut = statut; return this; }
        public Builder commentaires(String commentaires) { this.commentaires = commentaires; return this; }
        public Builder lastAccessedBy(Long lastAccessedBy) { this.lastAccessedBy = lastAccessedBy; return this; }
        public Builder lastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; return this; }

        public DossierMedicalDto build() {
            return new DossierMedicalDto(id, patientUserId, medecinUserId, createdAt, updatedAt,
                    analyseIds, ordonnanceIds, documentIds, noteIds, medecinsAutorisesIds,
                    resumeHistorique, statut, commentaires, lastAccessedBy, lastAccessedAt);
        }
    }
}
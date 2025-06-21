package com.cabinet360.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a medical note written by a doctor in a patient's medical record.
 */
@Entity
@Table(
        name = "note_medicale",
        indexes = {
                @Index(name = "idx_note_dossier", columnList = "dossier_medical_id"),
                @Index(name = "idx_note_medecin", columnList = "medecin_user_id")
        }
)
public class NoteMedicale {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Text content of the medical note
    @Column(length = 1000)
    private String contenu;

    // Timestamp when the note was created
    @Column(name = "date_note", nullable = false)
    private LocalDateTime dateNote = LocalDateTime.now();

    // ID of the doctor who wrote the note
    @Column(name = "medecin_user_id", nullable = false)
    private Long medecinUserId;

    // Relation to the patient's medical record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    // --- Constructors ---

    public NoteMedicale() {
    }

    public NoteMedicale(Long id, String contenu, LocalDateTime dateNote,
                        Long medecinUserId, DossierMedical dossierMedical) {
        this.id = id;
        this.contenu = contenu;
        this.dateNote = dateNote;
        this.medecinUserId = medecinUserId;
        this.dossierMedical = dossierMedical;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateNote() {
        return dateNote;
    }

    public void setDateNote(LocalDateTime dateNote) {
        this.dateNote = dateNote;
    }

    public Long getMedecinUserId() {
        return medecinUserId;
    }

    public void setMedecinUserId(Long medecinUserId) {
        this.medecinUserId = medecinUserId;
    }

    public DossierMedical getDossierMedical() {
        return dossierMedical;
    }

    public void setDossierMedical(DossierMedical dossierMedical) {
        this.dossierMedical = dossierMedical;
    }

    // --- equals and hashCode based on id ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoteMedicale)) return false;
        NoteMedicale that = (NoteMedicale) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- toString() for debugging/logging ---

    @Override
    public String toString() {
        return "NoteMedicale{" +
                "id=" + id +
                ", contenu='" + contenu + '\'' +
                ", dateNote=" + dateNote +
                ", medecinUserId=" + medecinUserId +
                '}';
    }

    // --- Builder pattern for object creation ---

    public static class Builder {
        private Long id;
        private String contenu;
        private LocalDateTime dateNote;
        private Long medecinUserId;
        private DossierMedical dossierMedical;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder contenu(String contenu) {
            this.contenu = contenu;
            return this;
        }

        public Builder dateNote(LocalDateTime dateNote) {
            this.dateNote = dateNote;
            return this;
        }

        public Builder medecinUserId(Long medecinUserId) {
            this.medecinUserId = medecinUserId;
            return this;
        }

        public Builder dossierMedical(DossierMedical dossierMedical) {
            this.dossierMedical = dossierMedical;
            return this;
        }

        public NoteMedicale build() {
            return new NoteMedicale(id, contenu, dateNote, medecinUserId, dossierMedical);
        }
    }
}
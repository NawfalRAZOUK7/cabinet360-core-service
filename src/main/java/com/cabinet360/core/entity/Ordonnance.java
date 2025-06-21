package com.cabinet360.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a prescription (Ordonnance) associated with a patient and doctor.
 */
@Entity
@Table(
        name = "ordonnance",
        indexes = {
                @Index(name = "idx_ordonnance_dossier", columnList = "dossier_medical_id"),
                @Index(name = "idx_ordonnance_patient", columnList = "patient_user_id"),
                @Index(name = "idx_ordonnance_medecin", columnList = "medecin_user_id")
        }
)
public class Ordonnance {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Date the prescription was created
    @Column(name = "date_ordonnance", nullable = false)
    private LocalDateTime dateOrdonnance;

    // Doctor's user ID who created the prescription
    @Column(name = "medecin_user_id", nullable = false)
    private Long medecinUserId;

    // Patient's user ID for whom the prescription is issued
    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    // Content of the prescription (medications, instructions, etc.)
    @Column(length = 1500)
    private String contenu;

    // Many-to-one relationship to DossierMedical
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    // --- Constructors ---

    // Default constructor (required by JPA)
    public Ordonnance() {
    }

    // All-args constructor
    public Ordonnance(Long id, LocalDateTime dateOrdonnance, Long medecinUserId, Long patientUserId,
                      String contenu, DossierMedical dossierMedical) {
        this.id = id;
        this.dateOrdonnance = dateOrdonnance;
        this.medecinUserId = medecinUserId;
        this.patientUserId = patientUserId;
        this.contenu = contenu;
        this.dossierMedical = dossierMedical;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateOrdonnance() {
        return dateOrdonnance;
    }

    public void setDateOrdonnance(LocalDateTime dateOrdonnance) {
        this.dateOrdonnance = dateOrdonnance;
    }

    public Long getMedecinUserId() {
        return medecinUserId;
    }

    public void setMedecinUserId(Long medecinUserId) {
        this.medecinUserId = medecinUserId;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public DossierMedical getDossierMedical() {
        return dossierMedical;
    }

    public void setDossierMedical(DossierMedical dossierMedical) {
        this.dossierMedical = dossierMedical;
    }

    // --- equals and hashCode based on 'id' ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ordonnance)) return false;
        Ordonnance that = (Ordonnance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- toString() for logging/debugging ---

    @Override
    public String toString() {
        return "Ordonnance{" +
                "id=" + id +
                ", dateOrdonnance=" + dateOrdonnance +
                ", medecinUserId=" + medecinUserId +
                ", patientUserId=" + patientUserId +
                ", contenu='" + contenu + '\'' +
                '}';
    }

    // --- Builder pattern for flexible object creation ---

    public static class Builder {
        private Long id;
        private LocalDateTime dateOrdonnance;
        private Long medecinUserId;
        private Long patientUserId;
        private String contenu;
        private DossierMedical dossierMedical;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder dateOrdonnance(LocalDateTime dateOrdonnance) {
            this.dateOrdonnance = dateOrdonnance;
            return this;
        }

        public Builder medecinUserId(Long medecinUserId) {
            this.medecinUserId = medecinUserId;
            return this;
        }

        public Builder patientUserId(Long patientUserId) {
            this.patientUserId = patientUserId;
            return this;
        }

        public Builder contenu(String contenu) {
            this.contenu = contenu;
            return this;
        }

        public Builder dossierMedical(DossierMedical dossierMedical) {
            this.dossierMedical = dossierMedical;
            return this;
        }

        public Ordonnance build() {
            return new Ordonnance(id, dateOrdonnance, medecinUserId, patientUserId, contenu, dossierMedical);
        }
    }
}
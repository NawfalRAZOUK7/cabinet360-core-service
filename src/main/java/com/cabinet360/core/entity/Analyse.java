package com.cabinet360.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a medical analysis associated with a patient and their dossier.
 * ✅ FIXED: Changed table name from 'analyse' to 'medical_analyses' to avoid PostgreSQL reserved keyword conflict
 */
@Entity
@Table(
        name = "medical_analyses", // ✅ FIXED: Changed from "analyse" (reserved keyword) to "medical_analyses"
        indexes = {
                @Index(name = "idx_medical_analyses_dossier", columnList = "dossier_medical_id"),
                @Index(name = "idx_medical_analyses_patient", columnList = "patient_user_id"),
                @Index(name = "idx_medical_analyses_type", columnList = "type_analyse"),
                @Index(name = "idx_medical_analyses_date", columnList = "date_analyse")
        }
)
public class Analyse {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type of analysis (e.g., blood test, X-ray, etc.)
    @Column(name = "type_analyse", nullable = false, length = 100)
    private String typeAnalyse;

    // Result of the analysis
    @Column(name = "resultat", length = 1000)
    private String resultat;

    // Date when the analysis was performed
    @Column(name = "date_analyse", nullable = false)
    private LocalDateTime dateAnalyse;

    // ID of the patient associated with this analysis
    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    // Many-to-one relationship to DossierMedical
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    // --- Constructors ---

    // Default constructor (required by JPA)
    public Analyse() {
    }

    // All-args constructor
    public Analyse(Long id, String typeAnalyse, String resultat, LocalDateTime dateAnalyse,
                   Long patientUserId, DossierMedical dossierMedical) {
        this.id = id;
        this.typeAnalyse = typeAnalyse;
        this.resultat = resultat;
        this.dateAnalyse = dateAnalyse;
        this.patientUserId = patientUserId;
        this.dossierMedical = dossierMedical;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeAnalyse() {
        return typeAnalyse;
    }

    public void setTypeAnalyse(String typeAnalyse) {
        this.typeAnalyse = typeAnalyse;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    public LocalDateTime getDateAnalyse() {
        return dateAnalyse;
    }

    public void setDateAnalyse(LocalDateTime dateAnalyse) {
        this.dateAnalyse = dateAnalyse;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
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
        if (!(o instanceof Analyse)) return false;
        Analyse analyse = (Analyse) o;
        return Objects.equals(id, analyse.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- toString() for debugging/logging ---

    @Override
    public String toString() {
        return "Analyse{" +
                "id=" + id +
                ", typeAnalyse='" + typeAnalyse + '\'' +
                ", resultat='" + resultat + '\'' +
                ", dateAnalyse=" + dateAnalyse +
                ", patientUserId=" + patientUserId +
                '}';
    }

    // --- Builder pattern for clean object creation ---

    public static class Builder {
        private Long id;
        private String typeAnalyse;
        private String resultat;
        private LocalDateTime dateAnalyse;
        private Long patientUserId;
        private DossierMedical dossierMedical;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder typeAnalyse(String typeAnalyse) {
            this.typeAnalyse = typeAnalyse;
            return this;
        }

        public Builder resultat(String resultat) {
            this.resultat = resultat;
            return this;
        }

        public Builder dateAnalyse(LocalDateTime dateAnalyse) {
            this.dateAnalyse = dateAnalyse;
            return this;
        }

        public Builder patientUserId(Long patientUserId) {
            this.patientUserId = patientUserId;
            return this;
        }

        public Builder dossierMedical(DossierMedical dossierMedical) {
            this.dossierMedical = dossierMedical;
            return this;
        }

        public Analyse build() {
            return new Analyse(id, typeAnalyse, resultat, dateAnalyse, patientUserId, dossierMedical);
        }
    }
}
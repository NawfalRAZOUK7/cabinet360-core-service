package com.cabinet360.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a medical record (DossierMedical) associated with a patient and a doctor.
 * It contains multiple medical components like analyses, prescriptions, documents, and notes.
 * ✅ Updated with all missing fields for proper service integration.
 */
@Entity
@Table(
        name = "dossier_medical",
        uniqueConstraints = @UniqueConstraint(columnNames = {"patient_user_id"}),
        indexes = {
                @Index(name = "idx_dossier_patient", columnList = "patient_user_id"),
                @Index(name = "idx_dossier_medecin", columnList = "medecin_user_id"),
                @Index(name = "idx_dossier_statut", columnList = "statut"),
                @Index(name = "idx_dossier_last_accessed", columnList = "last_accessed_at")
        }
)
public class DossierMedical {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique ID of the patient associated with this dossier
    @Column(name = "patient_user_id", nullable = false, unique = true)
    private Long patientUserId;

    // ID of the assigned doctor (can be null)
    @Column(name = "medecin_user_id")
    private Long medecinUserId;

    // Timestamp of when the record was created
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ✅ Added: Timestamp of when the record was last updated
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ Added: Historical summary of the patient's medical history
    @Column(name = "resume_historique", length = 2000)
    private String resumeHistorique;

    // ✅ Added: Status of the dossier (ACTIF, ARCHIVE, SUSPENDU)
    @Column(name = "statut", length = 50, nullable = false)
    private String statut = "ACTIF";

    // ✅ Added: Additional comments about the dossier
    @Column(name = "commentaires", length = 1000)
    private String commentaires;

    // ✅ Added: Audit trail - who last accessed this dossier
    @Column(name = "last_accessed_by")
    private Long lastAccessedBy;

    // ✅ Added: Audit trail - when was this dossier last accessed
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    // --- Relationships with other entities ---

    // List of analyses linked to this dossier
    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Analyse> analyses;

    // List of prescriptions linked to this dossier
    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Ordonnance> ordonnances;

    // List of medical documents linked to this dossier
    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Document> documents;

    // List of doctor notes linked to this dossier
    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoteMedicale> notes;

    // Authorized doctors who can access this dossier
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "dossier_medecin_access",
            joinColumns = @JoinColumn(name = "dossier_medical_id"),
            inverseJoinColumns = @JoinColumn(name = "medecin_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"dossier_medical_id", "medecin_id"}),
            indexes = {
                    @Index(name = "idx_dossier_access_dossier", columnList = "dossier_medical_id"),
                    @Index(name = "idx_dossier_access_medecin", columnList = "medecin_id")
            }
    )
    private List<Medecin> medecinsAutorises;

    // --- Constructors ---

    // Default constructor (needed by JPA)
    public DossierMedical() {}

    // Constructor with essential fields
    public DossierMedical(Long patientUserId, Long medecinUserId) {
        this.patientUserId = patientUserId;
        this.medecinUserId = medecinUserId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.statut = "ACTIF";
    }

    // Full constructor
    public DossierMedical(Long id, Long patientUserId, Long medecinUserId,
                          LocalDateTime createdAt, LocalDateTime updatedAt,
                          String resumeHistorique, String statut, String commentaires,
                          Long lastAccessedBy, LocalDateTime lastAccessedAt,
                          List<Analyse> analyses, List<Ordonnance> ordonnances,
                          List<Document> documents, List<NoteMedicale> notes,
                          List<Medecin> medecinsAutorises) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.medecinUserId = medecinUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resumeHistorique = resumeHistorique;
        this.statut = statut;
        this.commentaires = commentaires;
        this.lastAccessedBy = lastAccessedBy;
        this.lastAccessedAt = lastAccessedAt;
        this.analyses = analyses;
        this.ordonnances = ordonnances;
        this.documents = documents;
        this.notes = notes;
        this.medecinsAutorises = medecinsAutorises;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    public Long getMedecinUserId() { return medecinUserId; }
    public void setMedecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ✅ Added getters/setters for new fields
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

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

    // Existing relationship getters/setters
    public List<Analyse> getAnalyses() { return analyses; }
    public void setAnalyses(List<Analyse> analyses) { this.analyses = analyses; }

    public List<Ordonnance> getOrdonnances() { return ordonnances; }
    public void setOrdonnances(List<Ordonnance> ordonnances) { this.ordonnances = ordonnances; }

    public List<Document> getDocuments() { return documents; }
    public void setDocuments(List<Document> documents) { this.documents = documents; }

    public List<NoteMedicale> getNotes() { return notes; }
    public void setNotes(List<NoteMedicale> notes) { this.notes = notes; }

    public List<Medecin> getMedecinsAutorises() { return medecinsAutorises; }
    public void setMedecinsAutorises(List<Medecin> medecinsAutorises) { this.medecinsAutorises = medecinsAutorises; }

    // --- JPA Lifecycle callbacks ---

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (statut == null) {
            statut = "ACTIF";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Business methods ---

    /**
     * Checks if the dossier is active.
     */
    public boolean isActive() {
        return "ACTIF".equals(statut);
    }

    /**
     * Checks if the dossier is archived.
     */
    public boolean isArchived() {
        return "ARCHIVE".equals(statut);
    }

    /**
     * Checks if a doctor is authorized to access this dossier.
     */
    public boolean isDoctorAuthorized(Long doctorUserId) {
        // Check if it's the main assigned doctor
        if (doctorUserId.equals(medecinUserId)) {
            return true;
        }

        // Check if doctor is in authorized list
        return medecinsAutorises != null &&
                medecinsAutorises.stream()
                        .anyMatch(medecin -> doctorUserId.equals(medecin.getDoctorUserId()));
    }

    /**
     * Gets the total number of medical components.
     */
    public int getTotalMedicalComponentsCount() {
        int count = 0;
        if (analyses != null) count += analyses.size();
        if (ordonnances != null) count += ordonnances.size();
        if (documents != null) count += documents.size();
        if (notes != null) count += notes.size();
        return count;
    }

    /**
     * Updates the last accessed information.
     */
    public void updateLastAccessed(Long accessedBy) {
        this.lastAccessedAt = LocalDateTime.now();
        this.lastAccessedBy = accessedBy;
    }

    /**
     * Archives the dossier.
     */
    public void archive() {
        this.statut = "ARCHIVE";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates the dossier.
     */
    public void activate() {
        this.statut = "ACTIF";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suspends the dossier.
     */
    public void suspend() {
        this.statut = "SUSPENDU";
        this.updatedAt = LocalDateTime.now();
    }

    // --- equals and hashCode based on 'id' ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DossierMedical)) return false;
        DossierMedical that = (DossierMedical) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- toString() for easy logging/debugging ---

    @Override
    public String toString() {
        return "DossierMedical{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", medecinUserId=" + medecinUserId +
                ", statut='" + statut + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", totalComponents=" + getTotalMedicalComponentsCount() +
                '}';
    }

    // --- Builder pattern for clean and safe object creation ---

    public static class Builder {
        private Long id;
        private Long patientUserId;
        private Long medecinUserId;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private String resumeHistorique;
        private String statut = "ACTIF";
        private String commentaires;
        private Long lastAccessedBy;
        private LocalDateTime lastAccessedAt;
        private List<Analyse> analyses;
        private List<Ordonnance> ordonnances;
        private List<Document> documents;
        private List<NoteMedicale> notes;
        private List<Medecin> medecinsAutorises;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder patientUserId(Long patientUserId) { this.patientUserId = patientUserId; return this; }
        public Builder medecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder resumeHistorique(String resumeHistorique) { this.resumeHistorique = resumeHistorique; return this; }
        public Builder statut(String statut) { this.statut = statut; return this; }
        public Builder commentaires(String commentaires) { this.commentaires = commentaires; return this; }
        public Builder lastAccessedBy(Long lastAccessedBy) { this.lastAccessedBy = lastAccessedBy; return this; }
        public Builder lastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; return this; }
        public Builder analyses(List<Analyse> analyses) { this.analyses = analyses; return this; }
        public Builder ordonnances(List<Ordonnance> ordonnances) { this.ordonnances = ordonnances; return this; }
        public Builder documents(List<Document> documents) { this.documents = documents; return this; }
        public Builder notes(List<NoteMedicale> notes) { this.notes = notes; return this; }
        public Builder medecinsAutorises(List<Medecin> medecinsAutorises) { this.medecinsAutorises = medecinsAutorises; return this; }

        public DossierMedical build() {
            return new DossierMedical(id, patientUserId, medecinUserId, createdAt, updatedAt,
                    resumeHistorique, statut, commentaires, lastAccessedBy, lastAccessedAt,
                    analyses, ordonnances, documents, notes, medecinsAutorises);
        }
    }

    // --- Static factory methods ---

    /**
     * Creates a new dossier for a patient.
     */
    public static DossierMedical createForPatient(Long patientUserId) {
        return new DossierMedical(patientUserId, null);
    }

    /**
     * Creates a new dossier for a patient with assigned doctor.
     */
    public static DossierMedical createForPatientWithDoctor(Long patientUserId, Long medecinUserId) {
        return new DossierMedical(patientUserId, medecinUserId);
    }
}
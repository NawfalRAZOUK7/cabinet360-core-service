package com.cabinet360.core.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a medical document (e.g., scan, report, image)
 * associated with a patient's medical record.
 */
@Entity
@Table(
        name = "document",
        indexes = {
                @Index(name = "idx_document_dossier", columnList = "dossier_medical_id"),
                @Index(name = "idx_document_patient", columnList = "patient_user_id")
        }
)
public class Document {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // File name
    @Column(name = "nom", nullable = false, length = 255)
    private String nom;

    // URL to access the file
    @Column(name = "url", nullable = false, length = 500)
    private String url;

    // Document type (e.g., PDF, image)
    @Column(name = "type_document", length = 100)
    private String typeDocument;

    // ID of the patient who uploaded or received the document
    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    // Date and time when the document was uploaded
    @Column(name = "date_upload", nullable = false)
    private LocalDateTime dateUpload = LocalDateTime.now();

    // Relationship to DossierMedical
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)
    private DossierMedical dossierMedical;

    // --- Constructors ---

    public Document() {
    }

    public Document(Long id, String nom, String url, String typeDocument,
                    Long patientUserId, LocalDateTime dateUpload, DossierMedical dossierMedical) {
        this.id = id;
        this.nom = nom;
        this.url = url;
        this.typeDocument = typeDocument;
        this.patientUserId = patientUserId;
        this.dateUpload = dateUpload;
        this.dossierMedical = dossierMedical;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
    }

    public LocalDateTime getDateUpload() {
        return dateUpload;
    }

    public void setDateUpload(LocalDateTime dateUpload) {
        this.dateUpload = dateUpload;
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
        if (!(o instanceof Document)) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- toString() for logging/debugging ---

    @Override
    public String toString() {
        return "Document{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", url='" + url + '\'' +
                ", typeDocument='" + typeDocument + '\'' +
                ", patientUserId=" + patientUserId +
                ", dateUpload=" + dateUpload +
                '}';
    }

    // --- Builder pattern for object creation ---

    public static class Builder {
        private Long id;
        private String nom;
        private String url;
        private String typeDocument;
        private Long patientUserId;
        private LocalDateTime dateUpload;
        private DossierMedical dossierMedical;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder typeDocument(String typeDocument) {
            this.typeDocument = typeDocument;
            return this;
        }

        public Builder patientUserId(Long patientUserId) {
            this.patientUserId = patientUserId;
            return this;
        }

        public Builder dateUpload(LocalDateTime dateUpload) {
            this.dateUpload = dateUpload;
            return this;
        }

        public Builder dossierMedical(DossierMedical dossierMedical) {
            this.dossierMedical = dossierMedical;
            return this;
        }

        public Document build() {
            return new Document(id, nom, url, typeDocument, patientUserId, dateUpload, dossierMedical);
        }
    }
}
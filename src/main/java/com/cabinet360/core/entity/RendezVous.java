package com.cabinet360.core.entity;

import com.cabinet360.core.enums.RendezVousStatut;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entité JPA représentant un rendez-vous médical.
 * Contient les informations de base d'un rendez-vous entre un patient et un médecin.
 */
@Entity
@Table(name = "rendez_vous", indexes = {
        @Index(name = "idx_patient_user_id", columnList = "patient_user_id"),
        @Index(name = "idx_doctor_user_id", columnList = "doctor_user_id"),
        @Index(name = "idx_date_heure", columnList = "date_heure"),
        @Index(name = "idx_statut", columnList = "statut"),
        @Index(name = "idx_doctor_date_statut", columnList = "doctor_user_id, date_heure, statut"),
        @Index(name = "idx_patient_date_statut", columnList = "patient_user_id, date_heure, statut")
})
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID de l'utilisateur patient (référence vers auth-service)
     */
    @NotNull(message = "L'ID du patient est obligatoire")
    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    /**
     * ID de l'utilisateur médecin (référence vers auth-service)
     */
    @NotNull(message = "L'ID du médecin est obligatoire")
    @Column(name = "doctor_user_id", nullable = false)
    private Long medecinUserId;

    /**
     * Date et heure du rendez-vous
     */
    @NotNull(message = "La date et heure du rendez-vous sont obligatoires")
    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    /**
     * Statut du rendez-vous
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private RendezVousStatut statut = RendezVousStatut.CONFIRME;

    /**
     * Durée prévue du rendez-vous en minutes
     */
    @Positive(message = "La durée doit être positive")
    @Column(name = "duree_minutes")
    private Integer dureeMinutes = 30; // Durée par défaut : 30 minutes

    /**
     * Motif du rendez-vous (optionnel)
     */
    @Column(name = "motif", length = 500)
    private String motif;

    /**
     * Notes additionnelles (optionnel)
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Date de création de l'enregistrement
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================
    // CONSTRUCTORS
    // ========================================

    public RendezVous() {
        // Les timestamps sont maintenant gérés automatiquement par Hibernate
    }

    public RendezVous(Long id, Long patientUserId, Long medecinUserId, LocalDateTime dateHeure,
                      RendezVousStatut statut, Integer dureeMinutes) {
        this();
        this.id = id;
        this.patientUserId = patientUserId;
        this.medecinUserId = medecinUserId;
        this.dateHeure = dateHeure;
        this.statut = statut;
        this.dureeMinutes = dureeMinutes;
    }

    // ========================================
    // JPA LIFECYCLE METHODS
    // ========================================

    @PrePersist
    public void prePersist() {
        if (this.statut == null) {
            this.statut = RendezVousStatut.CONFIRME;
        }

        if (this.dureeMinutes == null) {
            this.dureeMinutes = 30;
        }
    }

    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
    }

    public Long getMedecinUserId() {
        return medecinUserId;
    }

    public void setMedecinUserId(Long medecinUserId) {
        this.medecinUserId = medecinUserId;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public RendezVousStatut getStatut() {
        return statut;
    }

    public void setStatut(RendezVousStatut statut) {
        this.statut = statut;
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Calcule l'heure de fin du rendez-vous
     */
    public LocalDateTime getHeureFinRendezVous() {
        if (dateHeure != null && dureeMinutes != null) {
            return dateHeure.plusMinutes(dureeMinutes);
        }
        return null;
    }

    /**
     * Vérifie si le rendez-vous est dans le futur
     */
    public boolean isUpcoming() {
        return dateHeure != null && dateHeure.isAfter(LocalDateTime.now());
    }

    /**
     * Vérifie si le rendez-vous est aujourd'hui
     */
    public boolean isToday() {
        if (dateHeure == null) return false;
        return dateHeure.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }

    /**
     * Vérifie si le rendez-vous peut être modifié
     */
    public boolean isModifiable() {
        return statut != null && statut.isModifiable() && isUpcoming();
    }

    /**
     * Vérifie si le rendez-vous peut être annulé
     */
    public boolean isCancellable() {
        return statut != null && statut.isCancellable() && isUpcoming();
    }

    /**
     * Retourne la durée formatée du rendez-vous
     */
    public String getDureeFormatee() {
        if (dureeMinutes == null) return "Non spécifiée";

        if (dureeMinutes < 60) {
            return dureeMinutes + " minutes";
        } else {
            int heures = dureeMinutes / 60;
            int minutes = dureeMinutes % 60;
            if (minutes == 0) {
                return heures + " heure" + (heures > 1 ? "s" : "");
            } else {
                return heures + "h" + String.format("%02d", minutes);
            }
        }
    }

    /**
     * Vérifie s'il y a conflit avec un autre rendez-vous
     */
    public boolean hasConflictWith(RendezVous other) {
        if (other == null || this.dateHeure == null || other.dateHeure == null) {
            return false;
        }

        // Même médecin ou même patient
        if (!this.medecinUserId.equals(other.medecinUserId) &&
                !this.patientUserId.equals(other.patientUserId)) {
            return false;
        }

        LocalDateTime thisStart = this.dateHeure;
        LocalDateTime thisEnd = this.getHeureFinRendezVous();
        LocalDateTime otherStart = other.dateHeure;
        LocalDateTime otherEnd = other.getHeureFinRendezVous();

        if (thisEnd == null || otherEnd == null) {
            return false;
        }

        // Vérification du chevauchement
        return thisStart.isBefore(otherEnd) && thisEnd.isAfter(otherStart);
    }

    // ========================================
    // EQUALS, HASHCODE, TOSTRING
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RendezVous that = (RendezVous) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", medecinUserId=" + medecinUserId +
                ", dateHeure=" + dateHeure +
                ", statut=" + statut +
                ", dureeMinutes=" + dureeMinutes +
                ", motif='" + motif + '\'' +
                ", isUpcoming=" + isUpcoming() +
                ", isToday=" + isToday() +
                '}';
    }
}
package com.cabinet360.core.dto;

import com.cabinet360.core.enums.RendezVousStatut;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO pour les rendez-vous médicaux.
 * Utilisé pour les échanges entre le frontend et l'API REST.
 * Contient des validations complètes et des champs calculés pour l'affichage.
 */
public class RendezVousDto {

    private Long id;

    @NotNull(message = "L'ID du patient est obligatoire")
    @Positive(message = "L'ID du patient doit être positif")
    @JsonProperty("patientUserId")
    private Long patientUserId;

    @NotNull(message = "L'ID du médecin est obligatoire")
    @Positive(message = "L'ID du médecin doit être positif")
    @JsonProperty("medecinUserId")
    private Long medecinUserId;

    @NotNull(message = "La date et heure du rendez-vous sont obligatoires")
    @Future(message = "La date du rendez-vous doit être dans le futur")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Paris")
    @JsonProperty("dateHeure")
    private LocalDateTime dateHeure;

    @NotNull(message = "Le statut est obligatoire")
    private RendezVousStatut statut = RendezVousStatut.CONFIRME;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 15, message = "La durée minimale est de 15 minutes")
    @Max(value = 480, message = "La durée maximale est de 8 heures (480 minutes)")
    @JsonProperty("dureeMinutes")
    private Integer dureeMinutes = 30;

    @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
    private String motif;

    @Size(max = 1000, message = "Les notes ne peuvent pas dépasser 1000 caractères")
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Paris")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Paris")
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    // ========================================
    // CHAMPS CALCULÉS (READ-ONLY)
    // ========================================

    @JsonProperty("heureFinRendezVous")
    private LocalDateTime heureFinRendezVous;

    @JsonProperty("isUpcoming")
    private boolean isUpcoming;

    @JsonProperty("isToday")
    private boolean isToday;

    @JsonProperty("isModifiable")
    private boolean isModifiable;

    @JsonProperty("isCancellable")
    private boolean isCancellable;

    @JsonProperty("dureeFormatee")
    private String dureeFormatee;

    @JsonProperty("statutLibelle")
    private String statutLibelle;

    @JsonProperty("statutCouleur")
    private String statutCouleur;

    @JsonProperty("statutIcone")
    private String statutIcone;

    @JsonProperty("timeUntilAppointment")
    private String timeUntilAppointment;

    // ========================================
    // CONSTRUCTORS
    // ========================================

    public RendezVousDto() {
        calculateDerivedFields();
    }

    public RendezVousDto(Long id, Long patientUserId, Long medecinUserId, LocalDateTime dateHeure,
                         RendezVousStatut statut, Integer dureeMinutes) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.medecinUserId = medecinUserId;
        this.dateHeure = dateHeure;
        this.statut = statut;
        this.dureeMinutes = dureeMinutes;
        calculateDerivedFields();
    }

    public RendezVousDto(Long id, Long patientUserId, Long medecinUserId, LocalDateTime dateHeure,
                         RendezVousStatut statut, Integer dureeMinutes, String motif, String notes,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.medecinUserId = medecinUserId;
        this.dateHeure = dateHeure;
        this.statut = statut;
        this.dureeMinutes = dureeMinutes;
        this.motif = motif;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        calculateDerivedFields();
    }

    // ========================================
    // CALCULATION METHODS
    // ========================================

    /**
     * Calcule les champs dérivés après modification des champs de base
     */
    private void calculateDerivedFields() {
        calculateTimeFields();
        calculateStatusFields();
        calculateDurationFields();
        calculateBusinessLogicFields();
    }

    private void calculateTimeFields() {
        if (dateHeure != null && dureeMinutes != null) {
            this.heureFinRendezVous = dateHeure.plusMinutes(dureeMinutes);
        }

        if (dateHeure != null) {
            LocalDateTime now = LocalDateTime.now();
            this.isUpcoming = dateHeure.isAfter(now);
            this.isToday = dateHeure.toLocalDate().equals(now.toLocalDate());
            this.timeUntilAppointment = calculateTimeUntil(now);
        }
    }

    private void calculateStatusFields() {
        if (statut != null) {
            this.statutLibelle = statut.getLibelle();
            this.statutCouleur = statut.getCouleur();
            this.statutIcone = statut.getIcone();
        }
    }

    private void calculateDurationFields() {
        this.dureeFormatee = formatDuration();
    }

    private void calculateBusinessLogicFields() {
        this.isModifiable = statut != null && statut.isModifiable() && isUpcoming;
        this.isCancellable = statut != null && statut.isCancellable() && isUpcoming;
    }

    private String calculateTimeUntil(LocalDateTime now) {
        if (dateHeure == null || !isUpcoming) {
            return null;
        }

        long minutesUntil = java.time.Duration.between(now, dateHeure).toMinutes();

        if (minutesUntil < 60) {
            return minutesUntil + " minutes";
        } else if (minutesUntil < 1440) { // moins de 24h
            long hours = minutesUntil / 60;
            return hours + " heure" + (hours > 1 ? "s" : "");
        } else {
            long days = minutesUntil / 1440;
            return days + " jour" + (days > 1 ? "s" : "");
        }
    }

    /**
     * Met à jour les champs calculés (à appeler après modification des champs de base)
     */
    public void updateCalculatedFields() {
        calculateDerivedFields();
    }

    // ========================================
    // BUSINESS LOGIC METHODS
    // ========================================

    /**
     * Retourne une représentation lisible de la durée
     */
    @JsonIgnore
    public String formatDuration() {
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
     * Vérifie si le rendez-vous est dans les prochaines 24 heures
     */
    @JsonProperty("isWithin24Hours")
    public boolean isWithin24Hours() {
        if (dateHeure == null) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        return dateHeure.isAfter(now) && dateHeure.isBefore(tomorrow);
    }

    /**
     * Vérifie si le rendez-vous est en retard (passé et pas terminé)
     */
    @JsonProperty("isOverdue")
    public boolean isOverdue() {
        if (dateHeure == null || statut == null) return false;
        return dateHeure.isBefore(LocalDateTime.now()) &&
                !statut.isFinal() &&
                statut != RendezVousStatut.EN_COURS;
    }

    /**
     * Retourne la priorité d'affichage du rendez-vous
     */
    @JsonProperty("displayPriority")
    public int getDisplayPriority() {
        if (statut == null) return 0;

        switch (statut) {
            case EN_COURS:
                return 10; // Priorité maximale
            case CONFIRME:
                return isWithin24Hours() ? 8 : 5;
            case EN_ATTENTE:
                return 7;
            case REPLANIFIE:
                return 6;
            case REPORTE:
                return 4;
            case ANNULE:
            case ABSENT:
            case TERMINE:
                return 1; // Priorité minimale
            default:
                return 0;
        }
    }

    /**
     * Retourne une description de l'état actuel pour l'utilisateur
     */
    @JsonProperty("userStatusMessage")
    public String getUserStatusMessage() {
        if (statut == null) return "Statut indéterminé";

        if (isOverdue()) {
            return "Rendez-vous en retard - Veuillez contacter le cabinet";
        }

        if (isWithin24Hours() && statut == RendezVousStatut.CONFIRME) {
            return "Rendez-vous dans les prochaines 24 heures";
        }

        return statut.getNotificationMessage();
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    /**
     * Validation personnalisée pour les règles métier
     */
    @AssertTrue(message = "La date du rendez-vous doit être dans le futur pour les nouveaux rendez-vous")
    @JsonIgnore
    public boolean isDateValidForNewAppointment() {
        // Pour les nouveaux RDV (id == null), la date doit être dans le futur
        if (id == null && dateHeure != null) {
            return dateHeure.isAfter(LocalDateTime.now());
        }
        return true; // Pour les RDV existants, on laisse plus de flexibilité
    }

    /**
     * Validation des heures ouvrables (8h-18h du lundi au samedi)
     */
    @AssertTrue(message = "Le rendez-vous doit être pris pendant les heures ouvrables (8h-18h, du lundi au samedi)")
    @JsonIgnore
    public boolean isWithinBusinessHours() {
        if (dateHeure == null) return true;

        int dayOfWeek = dateHeure.getDayOfWeek().getValue(); // 1=Lundi, 7=Dimanche
        int hour = dateHeure.getHour();

        // Pas le dimanche
        if (dayOfWeek == 7) return false;

        // Entre 8h et 18h
        return hour >= 8 && hour < 18;
    }

    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        updateCalculatedFields();
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
        updateCalculatedFields();
    }

    public Long getMedecinUserId() {
        return medecinUserId;
    }

    public void setMedecinUserId(Long medecinUserId) {
        this.medecinUserId = medecinUserId;
        updateCalculatedFields();
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
        updateCalculatedFields();
    }

    public RendezVousStatut getStatut() {
        return statut;
    }

    public void setStatut(RendezVousStatut statut) {
        this.statut = statut;
        updateCalculatedFields();
    }

    public Integer getDureeMinutes() {
        return dureeMinutes;
    }

    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
        updateCalculatedFields();
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

    // Getters pour les champs calculés (pas de setters car ils sont calculés automatiquement)

    public LocalDateTime getHeureFinRendezVous() {
        return heureFinRendezVous;
    }

    public boolean isUpcoming() {
        return isUpcoming;
    }

    public boolean isToday() {
        return isToday;
    }

    public boolean isModifiable() {
        return isModifiable;
    }

    public boolean isCancellable() {
        return isCancellable;
    }

    public String getDureeFormatee() {
        return dureeFormatee;
    }

    public String getStatutLibelle() {
        return statutLibelle;
    }

    public String getStatutCouleur() {
        return statutCouleur;
    }

    public String getStatutIcone() {
        return statutIcone;
    }

    public String getTimeUntilAppointment() {
        return timeUntilAppointment;
    }

    // ========================================
    // EQUALS, HASHCODE, TOSTRING
    // ========================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RendezVousDto that = (RendezVousDto) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "RendezVousDto{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", medecinUserId=" + medecinUserId +
                ", dateHeure=" + dateHeure +
                ", statut=" + statut +
                ", dureeMinutes=" + dureeMinutes +
                ", motif='" + motif + '\'' +
                ", isUpcoming=" + isUpcoming +
                ", isToday=" + isToday +
                ", statutLibelle='" + statutLibelle + '\'' +
                '}';
    }
}
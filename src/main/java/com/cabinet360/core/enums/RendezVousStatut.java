package com.cabinet360.core.enums;

/**
 * Énumération des statuts possibles pour un rendez-vous médical.
 * Utilisée pour suivre l'état d'un rendez-vous tout au long de son cycle de vie.
 */
public enum RendezVousStatut {

    /**
     * Rendez-vous en attente de confirmation
     */
    EN_ATTENTE("En attente", "Rendez-vous créé mais en attente de confirmation"),

    /**
     * Rendez-vous confirmé par les deux parties
     */
    CONFIRME("Confirmé", "Rendez-vous confirmé et programmé"),

    /**
     * Rendez-vous replanifié à une autre date/heure
     */
    REPLANIFIE("Replanifié", "Rendez-vous reporté à une autre date"),

    /**
     * Rendez-vous en cours de réalisation
     */
    EN_COURS("En cours", "Consultation en cours"),

    /**
     * Rendez-vous terminé avec succès
     */
    TERMINE("Terminé", "Consultation terminée"),

    /**
     * Rendez-vous annulé par le patient ou le médecin
     */
    ANNULE("Annulé", "Rendez-vous annulé"),

    /**
     * Patient absent lors du rendez-vous
     */
    ABSENT("Absent", "Patient absent lors du rendez-vous"),

    /**
     * Rendez-vous reporté à une date indéterminée
     */
    REPORTE("Reporté", "Rendez-vous reporté sans nouvelle date fixée");

    private final String libelle;
    private final String description;

    // ========================================
    // CONSTRUCTOR
    // ========================================

    RendezVousStatut(String libelle, String description) {
        this.libelle = libelle;
        this.description = description;
    }

    // ========================================
    // GETTERS
    // ========================================

    /**
     * Retourne le libellé français du statut
     */
    public String getLibelle() {
        return libelle;
    }

    /**
     * Retourne la description détaillée du statut
     */
    public String getDescription() {
        return description;
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Vérifie si le rendez-vous peut être modifié dans ce statut
     */
    public boolean isModifiable() {
        return this == EN_ATTENTE || this == CONFIRME || this == REPLANIFIE;
    }

    /**
     * Vérifie si le rendez-vous peut être annulé dans ce statut
     */
    public boolean isCancellable() {
        return this == EN_ATTENTE || this == CONFIRME || this == REPLANIFIE;
    }

    /**
     * Vérifie si le rendez-vous est dans un état final (terminé, annulé, etc.)
     */
    public boolean isFinal() {
        return this == TERMINE || this == ANNULE || this == ABSENT;
    }

    /**
     * Vérifie si le rendez-vous est actif (pas annulé ni terminé)
     */
    public boolean isActive() {
        return !isFinal();
    }

    /**
     * Vérifie si ce statut peut transitionner vers le statut cible
     */
    public boolean canTransitionTo(RendezVousStatut targetStatut) {
        if (targetStatut == null) return false;

        switch (this) {
            case EN_ATTENTE:
                return targetStatut == CONFIRME || targetStatut == ANNULE || targetStatut == REPLANIFIE;

            case CONFIRME:
                return targetStatut == EN_COURS || targetStatut == ANNULE ||
                        targetStatut == REPLANIFIE || targetStatut == ABSENT;

            case REPLANIFIE:
                return targetStatut == CONFIRME || targetStatut == ANNULE;

            case EN_COURS:
                return targetStatut == TERMINE || targetStatut == REPORTE;

            case TERMINE:
            case ANNULE:
            case ABSENT:
                return false; // États finaux, pas de transition possible

            case REPORTE:
                return targetStatut == CONFIRME || targetStatut == ANNULE;

            default:
                return false;
        }
    }

    /**
     * Retourne les statuts possibles après une transition depuis ce statut
     */
    public RendezVousStatut[] getPossibleTransitions() {
        switch (this) {
            case EN_ATTENTE:
                return new RendezVousStatut[]{CONFIRME, ANNULE, REPLANIFIE};

            case CONFIRME:
                return new RendezVousStatut[]{EN_COURS, ANNULE, REPLANIFIE, ABSENT};

            case REPLANIFIE:
                return new RendezVousStatut[]{CONFIRME, ANNULE};

            case EN_COURS:
                return new RendezVousStatut[]{TERMINE, REPORTE};

            case REPORTE:
                return new RendezVousStatut[]{CONFIRME, ANNULE};

            case TERMINE:
            case ANNULE:
            case ABSENT:
            default:
                return new RendezVousStatut[0]; // Aucune transition possible
        }
    }

    /**
     * Trouve un statut par son libellé
     */
    public static RendezVousStatut fromLibelle(String libelle) {
        if (libelle == null) return null;

        for (RendezVousStatut statut : values()) {
            if (statut.libelle.equalsIgnoreCase(libelle)) {
                return statut;
            }
        }
        return null;
    }

    /**
     * Retourne la couleur associée au statut (pour l'affichage UI)
     */
    public String getCouleur() {
        switch (this) {
            case EN_ATTENTE:
                return "#FFA500"; // Orange
            case CONFIRME:
                return "#28A745"; // Vert
            case REPLANIFIE:
                return "#17A2B8"; // Bleu cyan
            case EN_COURS:
                return "#007BFF"; // Bleu
            case TERMINE:
                return "#6C757D"; // Gris
            case ANNULE:
                return "#DC3545"; // Rouge
            case ABSENT:
                return "#6F42C1"; // Violet
            case REPORTE:
                return "#FD7E14"; // Orange foncé
            default:
                return "#6C757D"; // Gris par défaut
        }
    }

    /**
     * Retourne l'icône associée au statut (nom de l'icône FontAwesome ou autre)
     */
    public String getIcone() {
        switch (this) {
            case EN_ATTENTE:
                return "clock";
            case CONFIRME:
                return "check-circle";
            case REPLANIFIE:
                return "calendar-alt";
            case EN_COURS:
                return "play-circle";
            case TERMINE:
                return "check-double";
            case ANNULE:
                return "times-circle";
            case ABSENT:
                return "user-times";
            case REPORTE:
                return "pause-circle";
            default:
                return "question-circle";
        }
    }

    /**
     * Retourne une description courte du statut pour les notifications
     */
    public String getNotificationMessage() {
        switch (this) {
            case EN_ATTENTE:
                return "Votre rendez-vous est en attente de confirmation";
            case CONFIRME:
                return "Votre rendez-vous est confirmé";
            case REPLANIFIE:
                return "Votre rendez-vous a été replanifié";
            case EN_COURS:
                return "Votre consultation est en cours";
            case TERMINE:
                return "Votre consultation est terminée";
            case ANNULE:
                return "Votre rendez-vous a été annulé";
            case ABSENT:
                return "Absence notée pour ce rendez-vous";
            case REPORTE:
                return "Votre rendez-vous a été reporté";
            default:
                return "Statut du rendez-vous mis à jour";
        }
    }

    @Override
    public String toString() {
        return libelle;
    }
}
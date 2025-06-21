package com.cabinet360.core.dto;

import com.cabinet360.core.enums.RendezVousStatut;

import java.time.LocalDateTime;

public class RendezVousDto {

    private Long id;
    private Long patientUserId;
    private Long medecinUserId;
    private LocalDateTime dateHeure;
    private RendezVousStatut statut;
    private Integer dureeMinutes;

    public RendezVousDto() {}

    public RendezVousDto(Long id, Long patientUserId, Long medecinUserId, LocalDateTime dateHeure, RendezVousStatut statut, Integer dureeMinutes) {
        this.id = id;
        this.patientUserId = patientUserId;
        this.medecinUserId = medecinUserId;
        this.dateHeure = dateHeure;
        this.statut = statut;
        this.dureeMinutes = dureeMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    public Long getMedecinUserId() { return medecinUserId; }
    public void setMedecinUserId(Long medecinUserId) { this.medecinUserId = medecinUserId; }

    public LocalDateTime getDateHeure() { return dateHeure; }
    public void setDateHeure(LocalDateTime dateHeure) { this.dateHeure = dateHeure; }

    public RendezVousStatut getStatut() { return statut; }
    public void setStatut(RendezVousStatut statut) { this.statut = statut; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }
}

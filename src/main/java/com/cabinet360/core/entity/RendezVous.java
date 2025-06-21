package com.cabinet360.core.entity;

import com.cabinet360.core.enums.RendezVousStatut;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous")
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_user_id", nullable = false)
    private Long patientUserId;

    @Column(name = "doctor_user_id", nullable = false)
    private Long medecinUserId;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private RendezVousStatut statut;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    public RendezVous() {}

    public RendezVous(Long id, Long patientUserId, Long medecinUserId, LocalDateTime dateHeure, RendezVousStatut statut, Integer dureeMinutes) {
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

    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                ", medecinUserId=" + medecinUserId +
                ", dateHeure=" + dateHeure +
                ", statut='" + statut + '\'' +
                ", dureeMinutes=" + dureeMinutes +
                '}';
    }
}

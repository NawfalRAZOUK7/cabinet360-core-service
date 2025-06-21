package com.cabinet360.core.dto;

public class PatientDto {
    private Long patientUserId;
    private String nom;
    private String prenom;
    private Boolean actif;  // exemple de champ métier exposé

    // Constructeurs
    public PatientDto() {}

    public PatientDto(Long patientUserId, String nom, String prenom, Boolean actif) {
        this.patientUserId = patientUserId;
        this.nom = nom;
        this.prenom = prenom;
        this.actif = actif;
    }

    // Getters/Setters
    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
}

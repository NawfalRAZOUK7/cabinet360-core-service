package com.cabinet360.core.dto;

public class AssistantDto {

    private Long assistantUserId;

    // Champs métiers enrichis récupérés via auth-service
    private String nom;
    private String prenom;
    private Boolean actif;

    public AssistantDto() {}

    public AssistantDto(Long assistantUserId, String nom, String prenom, Boolean actif) {
        this.assistantUserId = assistantUserId;
        this.nom = nom;
        this.prenom = prenom;
        this.actif = actif;
    }

    public Long getAssistantUserId() {
        return assistantUserId;
    }

    public void setAssistantUserId(Long assistantUserId) {
        this.assistantUserId = assistantUserId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }
}

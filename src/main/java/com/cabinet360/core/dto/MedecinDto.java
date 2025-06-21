package com.cabinet360.core.dto;

public class MedecinDto {
    private Long doctorUserId;
    private String specialite;
    private Boolean isAvailable;

    // --- Constructeurs ---
    public MedecinDto() {}

    public MedecinDto(Long doctorUserId, String specialite, Boolean isAvailable) {
        this.doctorUserId = doctorUserId;
        this.specialite = specialite;
        this.isAvailable = isAvailable;
    }

    // --- Getters & Setters ---
    public Long getDoctorUserId() { return doctorUserId; }
    public void setDoctorUserId(Long doctorUserId) { this.doctorUserId = doctorUserId; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}

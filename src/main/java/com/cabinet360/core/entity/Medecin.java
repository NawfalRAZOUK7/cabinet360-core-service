package com.cabinet360.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "medecins",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medecin_user", columnNames = {"doctor_user_id"})
        },
        indexes = {
                @Index(name = "idx_doctor_user", columnList = "doctor_user_id")
        }
)
public class Medecin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "doctorUserId is required")
    @Column(name = "doctor_user_id", nullable = false, unique = true)
    private Long doctorUserId;

    // --- Constructors ---
    public Medecin() {}

    public Medecin(Long id, Long doctorUserId) {
        this.id = id;
        this.doctorUserId = doctorUserId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorUserId() { return doctorUserId; }
    public void setDoctorUserId(Long doctorUserId) { this.doctorUserId = doctorUserId; }

    @Override
    public String toString() {
        return "Medecin{" +
                "id=" + id +
                ", doctorUserId=" + doctorUserId +
                '}';
    }
}

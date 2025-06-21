package com.cabinet360.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "patients",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_patient_user", columnNames = {"patient_user_id"})
        },
        indexes = {
                @Index(name = "idx_patient_user", columnList = "patient_user_id")
        }
)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "patientUserId is required")
    @Column(name = "patient_user_id", nullable = false, unique = true)
    private Long patientUserId;

    // --- Constructors ---
    public Patient() {}

    public Patient(Long id, Long patientUserId) {
        this.id = id;
        this.patientUserId = patientUserId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", patientUserId=" + patientUserId +
                '}';
    }
}

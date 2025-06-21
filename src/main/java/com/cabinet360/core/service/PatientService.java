package com.cabinet360.core.service;

import com.cabinet360.core.client.AuthServiceClient;
import com.cabinet360.core.dto.PatientDto;
import com.cabinet360.core.entity.DossierMedical;
import com.cabinet360.core.entity.Patient;
import com.cabinet360.core.mapper.PatientMapper;
import com.cabinet360.core.repository.DossierMedicalRepository;
import com.cabinet360.core.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DossierMedicalRepository dossierMedicalRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    // --- CRUD local Patient (core-service) ---
    public PatientDto createPatient(PatientDto dto) {
        Patient patient = PatientMapper.toEntity(dto);
        Patient saved = patientRepository.save(patient);
        return PatientMapper.toDto(saved);
    }

    public PatientDto updatePatient(Long id, PatientDto dto) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient non trouvé"));
        existing.setPatientUserId(dto.getPatientUserId());
        Patient updated = patientRepository.save(existing);
        return PatientMapper.toDto(updated);
    }

    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient non trouvé");
        }
        patientRepository.deleteById(id);
    }

    public List<PatientDto> listAllPatients() {
        return patientRepository.findAll().stream()
                .map(PatientMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- Recherche patients avec dossier médical actif ---
    public List<PatientDto> findPatientsWithDossier() {
        List<Long> patientUserIdsWithDossier = dossierMedicalRepository.findAll().stream()
                .map(DossierMedical::getPatientUserId)
                .distinct()
                .collect(Collectors.toList());
        return patientRepository.findAll().stream()
                .filter(p -> patientUserIdsWithDossier.contains(p.getPatientUserId()))
                .map(PatientMapper::toDto)
                .collect(Collectors.toList());
    }

    // --- Recherche par patientUserId ---
    public PatientDto findByPatientUserId(Long patientUserId) {
        Patient patient = patientRepository.findByPatientUserId(patientUserId)
                .orElseThrow(() -> new IllegalArgumentException("Patient non trouvé"));
        return PatientMapper.toDto(patient);
    }

    // --- Recherche enrichie via auth-service ---
    public List<PatientDto> getPatientsFiltered(String nom, Boolean actif) {
        return authServiceClient.getPatientsByFilter(nom, actif);
    }
}

package com.cabinet360.core.service;

import com.cabinet360.core.client.AuthServiceClient;
import com.cabinet360.core.dto.MedecinDto;
import com.cabinet360.core.entity.Medecin;
import com.cabinet360.core.mapper.MedecinMapper;
import com.cabinet360.core.repository.MedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MedecinService {

    @Autowired
    private MedecinRepository medecinRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    // --- CRUD local sur entité Medecin légère ---
    public MedecinDto createMedecin(MedecinDto dto) {
        Medecin medecin = MedecinMapper.toEntity(dto);
        Medecin saved = medecinRepository.save(medecin);
        return MedecinMapper.toDto(saved);
    }

    public MedecinDto updateMedecin(Long id, MedecinDto dto) {
        Medecin existing = medecinRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé"));
        existing.setDoctorUserId(dto.getDoctorUserId());
        Medecin updated = medecinRepository.save(existing);
        return MedecinMapper.toDto(updated);
    }

    public void deleteMedecin(Long id) {
        if (!medecinRepository.existsById(id)) {
            throw new IllegalArgumentException("Médecin non trouvé");
        }
        medecinRepository.deleteById(id);
    }

    public List<MedecinDto> listAllMedecins() {
        return medecinRepository.findAll().stream()
                .map(MedecinMapper::toDto)
                .toList();
    }

    // --- Recherche enrichie via auth-service ---
    public List<MedecinDto> getMedecinsFiltered(Optional<String> specialite, Optional<Boolean> isAvailable) {
        return authServiceClient.getMedecinsByFilter(specialite, isAvailable);
    }

    // --- Recherche local par doctorUserId ---
    public MedecinDto findByDoctorUserId(Long doctorUserId) {
        Medecin medecin = medecinRepository.findByDoctorUserId(doctorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Médecin non trouvé"));
        return MedecinMapper.toDto(medecin);
    }
}

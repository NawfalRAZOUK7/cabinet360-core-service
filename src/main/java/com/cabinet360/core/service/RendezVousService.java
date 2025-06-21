package com.cabinet360.core.service;

import com.cabinet360.core.dto.RendezVousDto;
import com.cabinet360.core.entity.RendezVous;
import com.cabinet360.core.enums.RendezVousStatut;
import com.cabinet360.core.mapper.RendezVousMapper;
import com.cabinet360.core.repository.RendezVousRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RendezVousService {

    private static final Logger logger = LoggerFactory.getLogger(RendezVousService.class);

    @Autowired
    private RendezVousRepository rendezVousRepository;

    /**
     * Crée un rendez-vous après validation des conflits.
     * Log chaque création.
     */
    public RendezVousDto createRendezVous(RendezVousDto dto) {
        checkConflicts(dto);
        RendezVous entity = RendezVousMapper.toEntity(dto);
        entity.setStatut(RendezVousStatut.CONFIRME);
        RendezVous saved = rendezVousRepository.save(entity);
        logger.info("RDV créé: id={}, patient={}, medecin={}, date={}", saved.getId(), saved.getPatientUserId(), saved.getMedecinUserId(), saved.getDateHeure());
        return RendezVousMapper.toDto(saved);
    }

    /**
     * Met à jour un rendez-vous existant après validation des conflits ET du droit d’accès.
     * Seul le patient ou le médecin concerné peut modifier.
     * Log chaque update.
     */
    public RendezVousDto updateRendezVous(Long id, RendezVousDto dto, Long requestingUserId, String userRole) {
        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous non trouvé"));

        // Contrôle d'accès : seul le patient ou médecin concerné peut modifier
        if (!requestingUserId.equals(existing.getPatientUserId()) && !requestingUserId.equals(existing.getMedecinUserId())) {
            logger.warn("Tentative de modification illégale du RDV id={} par userId={}", id, requestingUserId);
            throw new SecurityException("Accès refusé");
        }

        checkConflicts(dto);

        existing.setPatientUserId(dto.getPatientUserId());
        existing.setMedecinUserId(dto.getMedecinUserId());
        existing.setDateHeure(dto.getDateHeure());
        existing.setStatut(dto.getStatut());
        existing.setDureeMinutes(dto.getDureeMinutes());

        RendezVous updated = rendezVousRepository.save(existing);
        logger.info("RDV modifié: id={}, modifié par userId={}, role={}", updated.getId(), requestingUserId, userRole);
        return RendezVousMapper.toDto(updated);
    }

    /**
     * Annule (logique) un rendez-vous par son ID : statut → CANCELED.
     * Seul le patient ou médecin concerné peut annuler.
     * Log chaque annulation.
     */
    public void cancelRendezVous(Long id, Long requestingUserId) {
        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous non trouvé"));

        if (!requestingUserId.equals(existing.getPatientUserId()) && !requestingUserId.equals(existing.getMedecinUserId())) {
            logger.warn("Tentative d'annulation illégale du RDV id={} par userId={}", id, requestingUserId);
            throw new SecurityException("Accès refusé");
        }

        existing.setStatut(RendezVousStatut.ANNULE);
        rendezVousRepository.save(existing);
        logger.info("RDV annulé (statut=CANCELED): id={}, par userId={}", id, requestingUserId);
    }

    /**
     * Liste tous les rendez-vous.
     */
    public List<RendezVousDto> listAllRendezVous() {
        return rendezVousRepository.findAll().stream()
                .map(RendezVousMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Liste les rendez-vous d’un patient.
     */
    public List<RendezVousDto> findByPatientUserId(Long patientUserId) {
        return rendezVousRepository.findByPatientUserId(patientUserId).stream()
                .map(RendezVousMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Liste les rendez-vous d’un médecin.
     */
    public List<RendezVousDto> findByMedecinUserId(Long medecinUserId) {
        return rendezVousRepository.findByMedecinUserId(medecinUserId).stream()
                .map(RendezVousMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Vérifie les conflits de planning avant création/modification.
     */
    private void checkConflicts(RendezVousDto dto) {
        Long medecinId = dto.getMedecinUserId();
        Long patientId = dto.getPatientUserId();
        LocalDateTime dateHeure = dto.getDateHeure();
        Integer duree = dto.getDureeMinutes() != null ? dto.getDureeMinutes() : 30;

        LocalDateTime fin = dateHeure.plusMinutes(duree);

        boolean conflitMedecin = rendezVousRepository.existsConflictingRdvMedecin(medecinId, dateHeure, fin);
        if (conflitMedecin) {
            throw new IllegalStateException("Conflit : médecin a déjà un rendez-vous à cette plage horaire");
        }

        boolean conflitPatient = rendezVousRepository.existsConflictingRdvPatient(patientId, dateHeure, fin);
        if (conflitPatient) {
            throw new IllegalStateException("Conflit : patient a déjà un rendez-vous à cette plage horaire");
        }
    }
}

package com.cabinet360.core.service;

import com.cabinet360.core.dto.RendezVousDto;
import com.cabinet360.core.entity.RendezVous;
import com.cabinet360.core.enums.RendezVousStatut;
import com.cabinet360.core.mapper.RendezVousMapper;
import com.cabinet360.core.repository.RendezVousRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service complet pour la gestion des rendez-vous médicaux.
 * Gère toutes les opérations CRUD, la logique métier, les conflits et les statistiques.
 */
@Service
@Transactional
public class RendezVousService {

    private static final Logger logger = LoggerFactory.getLogger(RendezVousService.class);

    @Autowired
    private RendezVousRepository rendezVousRepository;

    // ========================================
    // CREATE OPERATIONS
    // ========================================

    /**
     * Crée un nouveau rendez-vous après validation complète.
     * Vérifie les conflits, la validité des données et applique les règles métier.
     */
    @Transactional
    public RendezVousDto createRendezVous(RendezVousDto dto) {
        logger.info("🆕 Création d'un nouveau rendez-vous: patient={}, médecin={}, date={}",
                dto.getPatientUserId(), dto.getMedecinUserId(), dto.getDateHeure());

        // Validation métier
        validateRendezVousForCreation(dto);

        // Vérification des conflits
        checkConflicts(dto);

        // Nettoyage et préparation
        RendezVousDto sanitizedDto = RendezVousMapper.sanitizeDto(dto);
        RendezVous entity = RendezVousMapper.toEntity(sanitizedDto);

        // Statut par défaut
        if (entity.getStatut() == null) {
            entity.setStatut(RendezVousStatut.CONFIRME);
        }

        // Sauvegarde
        RendezVous saved = rendezVousRepository.save(entity);

        logger.info("✅ Rendez-vous créé avec succès: id={}, statut={}",
                saved.getId(), saved.getStatut());

        return RendezVousMapper.toDto(saved);
    }

    /**
     * Crée un rendez-vous avec vérification avancée de disponibilité
     */
    @Transactional
    public RendezVousDto createRendezVousWithSlotValidation(RendezVousDto dto) {
        logger.info("🆕 Création RDV avec validation de créneaux pour médecin: {}", dto.getMedecinUserId());

        // Vérifier que le créneau est libre
        List<LocalDateTime> availableSlots = findAvailableTimeSlots(
                dto.getMedecinUserId(),
                dto.getDateHeure().toLocalDate().toString(),
                dto.getDureeMinutes()
        );

        boolean slotAvailable = availableSlots.contains(dto.getDateHeure());
        if (!slotAvailable) {
            throw new IllegalStateException("Le créneau demandé n'est pas disponible");
        }

        return createRendezVous(dto);
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Trouve un rendez-vous par son ID avec gestion d'erreur
     */
    @Transactional(readOnly = true)
    public RendezVousDto findById(Long id) {
        logger.debug("🔍 Recherche du rendez-vous: {}", id);

        RendezVous rendezVous = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable avec l'ID: " + id));

        return RendezVousMapper.toDto(rendezVous);
    }

    /**
     * Liste tous les rendez-vous avec pagination
     */
    @Transactional(readOnly = true)
    public Page<RendezVousDto> listAllRendezVous(Pageable pageable) {
        logger.debug("📋 Récupération de tous les rendez-vous (page {})", pageable.getPageNumber());

        Page<RendezVous> page = rendezVousRepository.findAll(pageable);
        List<RendezVousDto> dtos = RendezVousMapper.toDtoList(page.getContent());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    /**
     * Liste tous les rendez-vous (sans pagination)
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> listAllRendezVous() {
        logger.debug("📋 Récupération de tous les rendez-vous");
        return RendezVousMapper.toDtoList(rendezVousRepository.findAll());
    }

    /**
     * Recherche les rendez-vous d'un patient
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByPatientUserId(Long patientUserId) {
        logger.debug("🔍 Recherche des RDV pour le patient: {}", patientUserId);

        List<RendezVous> appointments = rendezVousRepository.findByPatientUserId(patientUserId);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Recherche les rendez-vous d'un médecin
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByMedecinUserId(Long medecinUserId) {
        logger.debug("🔍 Recherche des RDV pour le médecin: {}", medecinUserId);

        List<RendezVous> appointments = rendezVousRepository.findByMedecinUserId(medecinUserId);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Recherche par statut
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByStatut(RendezVousStatut statut) {
        logger.debug("🔍 Recherche des RDV avec statut: {}", statut);

        List<RendezVous> appointments = rendezVousRepository.findByStatut(statut);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Recherche par date exacte
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByDateHeure(LocalDateTime dateHeure) {
        logger.debug("🔍 Recherche des RDV pour la date: {}", dateHeure);

        List<RendezVous> appointments = rendezVousRepository.findByDateHeure(dateHeure);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Recherche dans une plage de dates
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByDateRange(LocalDateTime start, LocalDateTime end) {
        logger.debug("🔍 Recherche des RDV entre {} et {}", start, end);

        List<RendezVous> appointments = rendezVousRepository.findByDateHeureBetween(start, end);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Recherche des RDV d'un patient dans une plage de dates
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByPatientUserIdAndDateHeureBetween(Long patientUserId, LocalDateTime start, LocalDateTime end) {
        logger.debug("🔍 Recherche des RDV du patient {} entre {} et {}", patientUserId, start, end);

        List<RendezVous> appointments = rendezVousRepository.findByPatientUserIdAndDateHeureBetween(patientUserId, start, end);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Recherche des RDV d'un médecin à une date donnée
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByMedecinUserIdAndDateHeure(Long medecinUserId, LocalDateTime dateHeure) {
        logger.debug("🔍 Recherche des RDV du médecin {} le {}", medecinUserId, dateHeure);

        List<RendezVous> appointments = rendezVousRepository.findByMedecinUserIdAndDateHeure(medecinUserId, dateHeure);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Trouve les prochains rendez-vous d'un patient
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findUpcomingAppointmentsForPatient(Long patientUserId) {
        logger.debug("🔍 Recherche des prochains RDV pour le patient: {}", patientUserId);

        LocalDateTime now = LocalDateTime.now();
        List<RendezVous> appointments = rendezVousRepository.findUpcomingAppointmentsByPatient(patientUserId, now);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Trouve les rendez-vous d'aujourd'hui pour un médecin
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findTodayAppointmentsForDoctor(Long medecinUserId) {
        logger.debug("🔍 Recherche des RDV d'aujourd'hui pour le médecin: {}", medecinUserId);

        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        List<RendezVous> appointments = rendezVousRepository.findTodayAppointmentsByDoctor(medecinUserId, startOfDay, endOfDay);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Trouve le prochain rendez-vous d'un patient
     */
    @Transactional(readOnly = true)
    public Optional<RendezVousDto> findNextAppointmentForPatient(Long patientUserId) {
        logger.debug("🔍 Recherche du prochain RDV pour le patient: {}", patientUserId);

        LocalDateTime now = LocalDateTime.now();
        Optional<RendezVous> nextAppointment = rendezVousRepository.findNextAppointmentByPatient(patientUserId, now);

        return nextAppointment.map(RendezVousMapper::toDto);
    }

    /**
     * Trouve le prochain rendez-vous d'un médecin
     */
    @Transactional(readOnly = true)
    public Optional<RendezVousDto> findNextAppointmentForDoctor(Long medecinUserId) {
        logger.debug("🔍 Recherche du prochain RDV pour le médecin: {}", medecinUserId);

        LocalDateTime now = LocalDateTime.now();
        Optional<RendezVous> nextAppointment = rendezVousRepository.findNextAppointmentByDoctor(medecinUserId, now);

        return nextAppointment.map(RendezVousMapper::toDto);
    }

    /**
     * Recherche les rendez-vous actifs (non terminés/annulés)
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findActiveAppointmentsByDoctor(Long medecinUserId) {
        logger.debug("🔍 Recherche des RDV actifs pour le médecin: {}", medecinUserId);

        List<RendezVous> appointments = rendezVousRepository.findActiveAppointmentsByDoctor(medecinUserId);
        return RendezVousMapper.toDtoList(appointments);
    }

    /**
     * Recherche par motif (recherche partielle)
     */
    @Transactional(readOnly = true)
    public List<RendezVousDto> findByMotifContaining(String motif) {
        logger.debug("🔍 Recherche des RDV avec motif contenant: {}", motif);

        List<RendezVous> appointments = rendezVousRepository.findByMotifContainingIgnoreCase(motif);
        return RendezVousMapper.toDtoList(appointments);
    }

    // ========================================
    // UPDATE OPERATIONS
    // ========================================

    /**
     * Met à jour un rendez-vous avec validation complète
     */
    @Transactional
    public RendezVousDto updateRendezVous(Long id, RendezVousDto dto, Long requestingUserId, String userRole) {
        logger.info("🔄 Mise à jour du RDV {} par l'utilisateur {} (rôle: {})", id, requestingUserId, userRole);

        // Récupération du RDV existant
        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        // Vérification des droits d'accès
        validateAccessRights(existing, requestingUserId, userRole);

        // Validation métier pour la mise à jour
        validateRendezVousForUpdate(dto, existing);

        // Vérification des conflits (en excluant le RDV actuel)
        dto.setId(id); // Important pour la vérification de conflit
        checkConflictsForUpdate(dto);

        // Détection des changements
        RendezVousDto originalDto = RendezVousMapper.toDto(existing);
        List<String> modifiedFields = RendezVousMapper.getModifiedFields(originalDto, dto);

        if (modifiedFields.isEmpty()) {
            logger.info("ℹ️ Aucune modification détectée pour le RDV {}", id);
            return originalDto;
        }

        // Application des modifications
        RendezVousMapper.updateEntityFromDto(existing, dto);
        RendezVous updated = rendezVousRepository.save(existing);

        logger.info("✅ RDV {} mis à jour. Champs modifiés: {}", id, String.join(", ", modifiedFields));
        return RendezVousMapper.toDto(updated);
    }

    /**
     * Met à jour uniquement le statut d'un rendez-vous
     */
    @Transactional
    public RendezVousDto updateStatus(Long id, RendezVousStatut newStatus, Long requestingUserId) {
        logger.info("🔄 Changement de statut du RDV {} vers {} par l'utilisateur {}", id, newStatus, requestingUserId);

        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        // Vérification basique des droits
        validateBasicAccessRights(existing, requestingUserId);

        // Validation de la transition de statut
        if (!existing.getStatut().canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Transition impossible de %s vers %s", existing.getStatut(), newStatus)
            );
        }

        RendezVousStatut oldStatus = existing.getStatut();
        existing.setStatut(newStatus);
        RendezVous updated = rendezVousRepository.save(existing);

        logger.info("✅ Statut du RDV {} changé de {} vers {}", id, oldStatus, newStatus);
        return RendezVousMapper.toDto(updated);
    }

    /**
     * Confirme un rendez-vous
     */
    @Transactional
    public RendezVousDto confirmAppointment(Long id, Long requestingUserId) {
        logger.info("✅ Confirmation du RDV {} par l'utilisateur {}", id, requestingUserId);
        return updateStatus(id, RendezVousStatut.CONFIRME, requestingUserId);
    }

    /**
     * Démarre un rendez-vous (passe en EN_COURS)
     */
    @Transactional
    public RendezVousDto startAppointment(Long id, Long requestingUserId) {
        logger.info("▶️ Démarrage du RDV {} par l'utilisateur {}", id, requestingUserId);
        return updateStatus(id, RendezVousStatut.EN_COURS, requestingUserId);
    }

    /**
     * Termine un rendez-vous
     */
    @Transactional
    public RendezVousDto completeAppointment(Long id, Long requestingUserId, String notes) {
        logger.info("🏁 Finalisation du RDV {} par l'utilisateur {}", id, requestingUserId);

        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        validateBasicAccessRights(existing, requestingUserId);

        existing.setStatut(RendezVousStatut.TERMINE);
        if (notes != null && !notes.trim().isEmpty()) {
            existing.setNotes(notes);
        }

        RendezVous updated = rendezVousRepository.save(existing);
        logger.info("✅ RDV {} terminé avec succès", id);

        return RendezVousMapper.toDto(updated);
    }

    /**
     * Replanifie un rendez-vous
     */
    @Transactional
    public RendezVousDto rescheduleAppointment(Long id, LocalDateTime newDateTime, Integer newDuration,
                                               Long requestingUserId, String userRole) {
        logger.info("🔄 Replanification du RDV {} pour le {} par l'utilisateur {}", id, newDateTime, requestingUserId);

        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        validateAccessRights(existing, requestingUserId, userRole);

        // Validation de la nouvelle date
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nouvelle date doit être dans le futur");
        }

        // Vérification des conflits pour le nouveau créneau
        RendezVousDto tempDto = new RendezVousDto();
        tempDto.setId(id);
        tempDto.setPatientUserId(existing.getPatientUserId());
        tempDto.setMedecinUserId(existing.getMedecinUserId());
        tempDto.setDateHeure(newDateTime);
        tempDto.setDureeMinutes(newDuration != null ? newDuration : existing.getDureeMinutes());

        checkConflictsForUpdate(tempDto);

        // Application des modifications
        existing.setDateHeure(newDateTime);
        if (newDuration != null) {
            existing.setDureeMinutes(newDuration);
        }
        existing.setStatut(RendezVousStatut.REPLANIFIE);

        RendezVous updated = rendezVousRepository.save(existing);
        logger.info("✅ RDV {} replanifié avec succès pour le {}", id, newDateTime);

        return RendezVousMapper.toDto(updated);
    }

    // ========================================
    // DELETE OPERATIONS
    // ========================================

    /**
     * Annule un rendez-vous (soft delete)
     */
    @Transactional
    public void cancelRendezVous(Long id, Long requestingUserId) {
        logger.info("❌ Annulation du RDV {} par l'utilisateur {}", id, requestingUserId);

        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        validateBasicAccessRights(existing, requestingUserId);

        if (!existing.getStatut().isCancellable()) {
            throw new IllegalStateException("Ce rendez-vous ne peut pas être annulé dans son état actuel: " + existing.getStatut());
        }

        existing.setStatut(RendezVousStatut.ANNULE);
        rendezVousRepository.save(existing);

        logger.info("✅ RDV {} annulé avec succès", id);
    }

    /**
     * Suppression définitive d'un rendez-vous (à utiliser avec précaution)
     */
    @Transactional
    public void deleteRendezVousPermanently(Long id, Long requestingUserId, String userRole) {
        logger.warn("🗑️ Suppression définitive du RDV {} par l'utilisateur {} (rôle: {})", id, requestingUserId, userRole);

        // Seuls les administrateurs peuvent supprimer définitivement
        if (!"ADMIN".equals(userRole)) {
            throw new SecurityException("Seuls les administrateurs peuvent supprimer définitivement un rendez-vous");
        }

        RendezVous existing = rendezVousRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rendez-vous introuvable"));

        rendezVousRepository.delete(existing);
        logger.warn("⚠️ RDV {} supprimé définitivement par {}", id, requestingUserId);
    }

    // ========================================
    // CONFLICT DETECTION & VALIDATION
    // ========================================

    /**
     * Vérifie les conflits pour un nouveau rendez-vous
     */
    public Map<String, Object> checkConflicts(RendezVousDto dto) {
        logger.debug("🔍 Vérification des conflits pour: médecin={}, patient={}, date={}",
                dto.getMedecinUserId(), dto.getPatientUserId(), dto.getDateHeure());

        Map<String, Object> conflicts = new HashMap<>();

        Long medecinId = dto.getMedecinUserId();
        Long patientId = dto.getPatientUserId();
        LocalDateTime dateHeure = dto.getDateHeure();
        Integer duree = dto.getDureeMinutes() != null ? dto.getDureeMinutes() : 30;

        LocalDateTime fin = dateHeure.plusMinutes(duree);

        boolean conflitMedecin = rendezVousRepository.existsConflictingRdvMedecin(medecinId, dateHeure, fin);
        boolean conflitPatient = rendezVousRepository.existsConflictingRdvPatient(patientId, dateHeure, fin);

        conflicts.put("doctorConflict", conflitMedecin);
        conflicts.put("patientConflict", conflitPatient);
        conflicts.put("hasConflicts", conflitMedecin || conflitPatient);

        if (conflitMedecin) {
            logger.warn("⚠️ Conflit détecté pour le médecin {} à {}", medecinId, dateHeure);
            throw new IllegalStateException("Conflit : médecin a déjà un rendez-vous à cette plage horaire");
        }

        if (conflitPatient) {
            logger.warn("⚠️ Conflit détecté pour le patient {} à {}", patientId, dateHeure);
            throw new IllegalStateException("Conflit : patient a déjà un rendez-vous à cette plage horaire");
        }

        logger.debug("✅ Aucun conflit détecté");
        return conflicts;
    }

    /**
     * Vérifie les conflits pour une mise à jour (exclut le RDV actuel)
     */
    private void checkConflictsForUpdate(RendezVousDto dto) {
        Long medecinId = dto.getMedecinUserId();
        Long patientId = dto.getPatientUserId();
        LocalDateTime dateHeure = dto.getDateHeure();
        Integer duree = dto.getDureeMinutes() != null ? dto.getDureeMinutes() : 30;
        Long excludeId = dto.getId();

        LocalDateTime fin = dateHeure.plusMinutes(duree);

        boolean conflitMedecin = rendezVousRepository.existsConflictingRdvMedecinExcluding(
                medecinId, dateHeure, fin, excludeId);
        boolean conflitPatient = rendezVousRepository.existsConflictingRdvPatientExcluding(
                patientId, dateHeure, fin, excludeId);

        if (conflitMedecin) {
            throw new IllegalStateException("Conflit : médecin a déjà un rendez-vous à cette plage horaire");
        }

        if (conflitPatient) {
            throw new IllegalStateException("Conflit : patient a déjà un rendez-vous à cette plage horaire");
        }
    }

    // ========================================
    // AVAILABLE SLOTS
    // ========================================

    /**
     * Trouve les créneaux disponibles pour un médecin
     */
    @Transactional(readOnly = true)
    public List<LocalDateTime> findAvailableTimeSlots(Long medecinUserId, String dateStr, Integer durationMinutes) {
        logger.debug("🔍 Recherche des créneaux libres pour le médecin {} le {} (durée: {}min)",
                medecinUserId, dateStr, durationMinutes);

        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime startOfDay = date.atTime(8, 0); // 8h
        LocalDateTime endOfDay = date.atTime(18, 0);   // 18h

        // Ne pas proposer des créneaux dans le passé
        LocalDateTime now = LocalDateTime.now();
        if (startOfDay.isBefore(now)) {
            startOfDay = now.plusMinutes(30); // Buffer de 30 minutes
        }

        List<LocalDateTime> availableSlots = new ArrayList<>();
        List<RendezVous> existingAppointments = rendezVousRepository.findByMedecinUserIdAndDateRange(
                medecinUserId, date.atTime(0, 0), date.atTime(23, 59));

        // Filtrer les RDV non annulés
        List<RendezVous> activeAppointments = existingAppointments.stream()
                .filter(rdv -> !rdv.getStatut().equals(RendezVousStatut.ANNULE))
                .collect(Collectors.toList());

        // Générer les créneaux possibles
        LocalDateTime currentSlot = startOfDay;
        while (currentSlot.plusMinutes(durationMinutes).isBefore(endOfDay) ||
                currentSlot.plusMinutes(durationMinutes).equals(endOfDay)) {

            final LocalDateTime slotStart = currentSlot;
            final LocalDateTime slotEnd = currentSlot.plusMinutes(durationMinutes);

            // Vérifier les conflits
            boolean isAvailable = activeAppointments.stream().noneMatch(rdv -> {
                LocalDateTime rdvStart = rdv.getDateHeure();
                LocalDateTime rdvEnd = rdv.getDateHeure().plusMinutes(rdv.getDureeMinutes());

                // Vérification du chevauchement
                return (slotStart.isBefore(rdvEnd) && slotEnd.isAfter(rdvStart));
            });

            if (isAvailable) {
                availableSlots.add(currentSlot);
            }

            currentSlot = currentSlot.plusMinutes(30); // Créneaux de 30 min
        }

        logger.debug("✅ {} créneaux disponibles trouvés", availableSlots.size());
        return availableSlots;
    }

    /**
     * Obtient les créneaux disponibles formatés pour l'affichage
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFormattedAvailableSlots(Long medecinUserId, String dateStr, Integer durationMinutes) {
        List<LocalDateTime> slots = findAvailableTimeSlots(medecinUserId, dateStr, durationMinutes);

        Map<String, Object> result = new HashMap<>();
        result.put("date", dateStr);
        result.put("doctorId", medecinUserId);
        result.put("duration", durationMinutes);
        result.put("totalSlots", slots.size());

        List<Map<String, String>> formattedSlots = slots.stream()
                .map(slot -> {
                    Map<String, String> slotInfo = new HashMap<>();
                    slotInfo.put("time", slot.format(DateTimeFormatter.ofPattern("HH:mm")));
                    slotInfo.put("datetime", slot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                    slotInfo.put("displayTime", slot.format(DateTimeFormatter.ofPattern("HH:mm")));
                    return slotInfo;
                })
                .collect(Collectors.toList());

        result.put("availableSlots", formattedSlots);

        return result;
    }

    // ========================================
    // STATISTICS & ANALYTICS
    // ========================================

    /**
     * Statistiques générales des rendez-vous
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAppointmentStats() {
        logger.debug("📊 Génération des statistiques générales");

        Map<String, Object> stats = new HashMap<>();

        Object[] globalStats = rendezVousRepository.getGlobalStatistics();
        if (globalStats != null && globalStats.length >= 4) {
            stats.put("totalAppointments", globalStats[0]);
            stats.put("confirmedAppointments", globalStats[1]);
            stats.put("cancelledAppointments", globalStats[2]);
            stats.put("completedAppointments", globalStats[3]);
        }

        // Statistiques du mois actuel
        LocalDateTime startOfMonth = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        Long thisMonthCount = rendezVousRepository.countByDateHeureBetween(startOfMonth, endOfMonth);
        stats.put("thisMonthAppointments", thisMonthCount);

        // Statistiques d'aujourd'hui
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long todayCount = rendezVousRepository.countByDateHeureBetween(startOfDay, endOfDay);
        stats.put("todayAppointments", todayCount);

        // Top des créneaux populaires
        List<Object[]> popularSlots = rendezVousRepository.findMostPopularTimeSlots();
        stats.put("popularTimeSlots", popularSlots);

        logger.debug("✅ Statistiques générées: {} RDV total", stats.get("totalAppointments"));
        return stats;
    }

    /**
     * Statistiques pour un médecin spécifique
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDoctorAppointmentStats(Long medecinUserId) {
        logger.debug("📊 Génération des statistiques pour le médecin: {}", medecinUserId);

        Map<String, Object> stats = new HashMap<>();

        Object[] doctorStats = rendezVousRepository.getDoctorStatistics(medecinUserId);
        if (doctorStats != null && doctorStats.length >= 4) {
            stats.put("totalAppointments", doctorStats[0]);
            stats.put("confirmedAppointments", doctorStats[1]);
            stats.put("cancelledAppointments", doctorStats[2]);
            stats.put("completedAppointments", doctorStats[3]);
        }

        // RDV d'aujourd'hui
        List<RendezVousDto> todayAppointments = findTodayAppointmentsForDoctor(medecinUserId);
        stats.put("todayAppointments", todayAppointments.size());
        stats.put("todayAppointmentsList", RendezVousMapper.toCalendarDtoList(
                todayAppointments.stream().map(dto -> RendezVousMapper.toEntity(dto)).collect(Collectors.toList())
        ));

        // Prochain RDV
        Optional<RendezVousDto> nextAppointment = findNextAppointmentForDoctor(medecinUserId);
        stats.put("nextAppointment", nextAppointment.orElse(null));

        // RDV actifs
        List<RendezVousDto> activeAppointments = findActiveAppointmentsByDoctor(medecinUserId);
        stats.put("activeAppointments", activeAppointments.size());

        logger.debug("✅ Statistiques médecin générées: {} RDV total", stats.get("totalAppointments"));
        return stats;
    }

    /**
     * Tableau de bord complet pour un médecin
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDoctorDashboard(Long medecinUserId) {
        logger.debug("📋 Génération du tableau de bord pour le médecin: {}", medecinUserId);

        Map<String, Object> dashboard = new HashMap<>();

        // Statistiques de base
        dashboard.putAll(getDoctorAppointmentStats(medecinUserId));

        // RDV de la semaine
        LocalDateTime startOfWeek = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        List<RendezVous> weekAppointments = rendezVousRepository.findByMedecinUserIdAndDateHeureBetween(medecinUserId, startOfWeek, endOfWeek);
        dashboard.put("weekAppointments", RendezVousMapper.toCalendarDtoList(weekAppointments));

        // Créneaux disponibles demain
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<LocalDateTime> tomorrowSlots = findAvailableTimeSlots(medecinUserId, tomorrow.toString(), 30);
        dashboard.put("tomorrowAvailableSlots", tomorrowSlots.size());

        // RDV en retard
        List<RendezVous> overdueAppointments = rendezVousRepository.findByMedecinUserId(medecinUserId).stream()
                .filter(rdv -> rdv.getDateHeure().isBefore(LocalDateTime.now()) &&
                        !rdv.getStatut().isFinal() &&
                        rdv.getStatut() != RendezVousStatut.EN_COURS)
                .collect(Collectors.toList());
        dashboard.put("overdueAppointments", RendezVousMapper.toDtoList(overdueAppointments));

        return dashboard;
    }

    // ========================================
    // MAINTENANCE & CLEANUP
    // ========================================

    /**
     * Nettoie les anciens rendez-vous annulés
     */
    @Transactional
    public int cleanupOldCancelledAppointments(int daysOld) {
        logger.info("🧹 Nettoyage des RDV annulés de plus de {} jours", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = rendezVousRepository.deleteOldCancelledAppointments(cutoffDate);

        logger.info("✅ {} anciens RDV annulés supprimés", deletedCount);
        return deletedCount;
    }

    /**
     * Archive les anciens rendez-vous terminés
     */
    @Transactional
    public int archiveOldCompletedAppointments(int daysOld) {
        logger.info("📦 Archivage des RDV terminés de plus de {} jours", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int archivedCount = rendezVousRepository.deleteOldCompletedAppointments(cutoffDate);

        logger.info("✅ {} anciens RDV terminés archivés", archivedCount);
        return archivedCount;
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    private void validateRendezVousForCreation(RendezVousDto dto) {
        if (!RendezVousMapper.isValidForCreation(dto)) {
            throw new IllegalArgumentException("Données insuffisantes pour créer le rendez-vous");
        }

        if (dto.getDateHeure().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La date du rendez-vous doit être dans le futur");
        }
    }

    private void validateRendezVousForUpdate(RendezVousDto dto, RendezVous existing) {
        if (!RendezVousMapper.isValidForUpdate(dto)) {
            throw new IllegalArgumentException("Données insuffisantes pour mettre à jour le rendez-vous");
        }

        if (!existing.getStatut().isModifiable()) {
            throw new IllegalStateException("Ce rendez-vous ne peut plus être modifié dans son état actuel: " + existing.getStatut());
        }
    }

    private void validateAccessRights(RendezVous rendezVous, Long requestingUserId, String userRole) {
        boolean isAdmin = "ADMIN".equals(userRole);
        boolean isAssistant = "ASSISTANT".equals(userRole);
        boolean isDoctor = "DOCTOR".equals(userRole) && requestingUserId.equals(rendezVous.getMedecinUserId());
        boolean isPatient = "PATIENT".equals(userRole) && requestingUserId.equals(rendezVous.getPatientUserId());

        if (!isAdmin && !isAssistant && !isDoctor && !isPatient) {
            throw new SecurityException("Accès refusé : vous n'avez pas les droits pour modifier ce rendez-vous");
        }
    }

    private void validateBasicAccessRights(RendezVous rendezVous, Long requestingUserId) {
        boolean hasAccess = requestingUserId.equals(rendezVous.getPatientUserId()) ||
                requestingUserId.equals(rendezVous.getMedecinUserId());

        if (!hasAccess) {
            throw new SecurityException("Accès refusé : seuls le patient et le médecin concernés peuvent modifier ce rendez-vous");
        }
    }
}
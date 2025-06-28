package com.cabinet360.core.repository;

import com.cabinet360.core.entity.RendezVous;
import com.cabinet360.core.enums.RendezVousStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des rendez-vous médicaux.
 * Contient toutes les méthodes de recherche et de statistiques pour les rendez-vous.
 */
@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    // ========================================
    // BASIC FIND METHODS
    // ========================================

    /**
     * Recherche tous les rendez-vous d'un patient donné.
     *
     * @param patientUserId l'ID du patient (auth-service)
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByPatientUserId(Long patientUserId);

    /**
     * Recherche tous les rendez-vous d'un médecin donné.
     *
     * @param medecinUserId l'ID du médecin (auth-service)
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByMedecinUserId(Long medecinUserId);

    /**
     * Recherche les rendez-vous d'un médecin à une date donnée.
     *
     * @param medecinUserId l'ID du médecin
     * @param dateHeure la date recherchée
     * @return Liste des rendez-vous correspondants
     */
    List<RendezVous> findByMedecinUserIdAndDateHeure(Long medecinUserId, LocalDateTime dateHeure);

    /**
     * Recherche tous les rendez-vous d'un patient sur une période donnée.
     *
     * @param patientUserId l'ID du patient
     * @param startDate début période
     * @param endDate fin période
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByPatientUserIdAndDateHeureBetween(Long patientUserId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Recherche tous les rendez-vous par statut (ex : CONFIRME, ANNULE, etc.)
     *
     * @param statut le statut du rendez-vous
     * @return Liste des rendez-vous correspondants
     */
    List<RendezVous> findByStatut(RendezVousStatut statut);

    /**
     * Recherche tous les rendez-vous à une date donnée.
     *
     * @param dateHeure la date recherchée
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByDateHeure(LocalDateTime dateHeure);

    // ========================================
    // ADVANCED FIND METHODS
    // ========================================

    /**
     * Recherche tous les rendez-vous dans une plage de dates.
     *
     * @param startDate début période
     * @param endDate fin période
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByDateHeureBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Recherche les rendez-vous d'un médecin dans une plage de dates.
     *
     * @param medecinUserId l'ID du médecin
     * @param startDate début période
     * @param endDate fin période
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByMedecinUserIdAndDateHeureBetween(Long medecinUserId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Recherche les rendez-vous d'un médecin avec un statut spécifique.
     *
     * @param medecinUserId l'ID du médecin
     * @param statut le statut recherché
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByMedecinUserIdAndStatut(Long medecinUserId, RendezVousStatut statut);

    /**
     * Recherche les rendez-vous d'un patient avec un statut spécifique.
     *
     * @param patientUserId l'ID du patient
     * @param statut le statut recherché
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByPatientUserIdAndStatut(Long patientUserId, RendezVousStatut statut);

    /**
     * Recherche les rendez-vous par motif (recherche partielle, insensible à la casse).
     *
     * @param motif le motif recherché
     * @return Liste des rendez-vous correspondants
     */
    List<RendezVous> findByMotifContainingIgnoreCase(String motif);

    // ========================================
    // CUSTOM QUERIES FOR SPECIFIC NEEDS
    // ========================================

    /**
     * Trouve les prochains rendez-vous d'un patient (non annulés).
     *
     * @param patientUserId l'ID du patient
     * @param currentDateTime date/heure actuelle
     * @return Liste des prochains rendez-vous
     */
    @Query("SELECT r FROM RendezVous r WHERE r.patientUserId = :patientUserId " +
            "AND r.dateHeure >= :currentDateTime " +
            "AND r.statut != 'ANNULE' " +
            "ORDER BY r.dateHeure ASC")
    List<RendezVous> findUpcomingAppointmentsByPatient(@Param("patientUserId") Long patientUserId,
                                                       @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Trouve les rendez-vous d'aujourd'hui pour un médecin (non annulés).
     *
     * @param medecinUserId l'ID du médecin
     * @param startOfDay début de la journée
     * @param endOfDay fin de la journée
     * @return Liste des rendez-vous d'aujourd'hui
     */
    @Query("SELECT r FROM RendezVous r WHERE r.medecinUserId = :medecinUserId " +
            "AND r.dateHeure >= :startOfDay " +
            "AND r.dateHeure <= :endOfDay " +
            "AND r.statut != 'ANNULE' " +
            "ORDER BY r.dateHeure ASC")
    List<RendezVous> findTodayAppointmentsByDoctor(@Param("medecinUserId") Long medecinUserId,
                                                   @Param("startOfDay") LocalDateTime startOfDay,
                                                   @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Trouve les rendez-vous actifs (non annulés, non terminés) pour un médecin.
     *
     * @param medecinUserId l'ID du médecin
     * @return Liste des rendez-vous actifs
     */
    @Query("SELECT r FROM RendezVous r WHERE r.medecinUserId = :medecinUserId " +
            "AND r.statut NOT IN ('ANNULE', 'TERMINE', 'ABSENT') " +
            "ORDER BY r.dateHeure ASC")
    List<RendezVous> findActiveAppointmentsByDoctor(@Param("medecinUserId") Long medecinUserId);

    /**
     * Trouve les rendez-vous actifs (non annulés, non terminés) pour un patient.
     *
     * @param patientUserId l'ID du patient
     * @return Liste des rendez-vous actifs
     */
    @Query("SELECT r FROM RendezVous r WHERE r.patientUserId = :patientUserId " +
            "AND r.statut NOT IN ('ANNULE', 'TERMINE', 'ABSENT') " +
            "ORDER BY r.dateHeure ASC")
    List<RendezVous> findActiveAppointmentsByPatient(@Param("patientUserId") Long patientUserId);

    /**
     * Trouve le prochain rendez-vous d'un patient.
     *
     * @param patientUserId l'ID du patient
     * @param currentDateTime date/heure actuelle
     * @return Le prochain rendez-vous ou empty si aucun
     */
    @Query("SELECT r FROM RendezVous r WHERE r.patientUserId = :patientUserId " +
            "AND r.dateHeure >= :currentDateTime " +
            "AND r.statut NOT IN ('ANNULE', 'TERMINE', 'ABSENT') " +
            "ORDER BY r.dateHeure ASC")
    Optional<RendezVous> findNextAppointmentByPatient(@Param("patientUserId") Long patientUserId,
                                                      @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Trouve le prochain rendez-vous d'un médecin.
     *
     * @param medecinUserId l'ID du médecin
     * @param currentDateTime date/heure actuelle
     * @return Le prochain rendez-vous ou empty si aucun
     */
    @Query("SELECT r FROM RendezVous r WHERE r.medecinUserId = :medecinUserId " +
            "AND r.dateHeure >= :currentDateTime " +
            "AND r.statut NOT IN ('ANNULE', 'TERMINE', 'ABSENT') " +
            "ORDER BY r.dateHeure ASC")
    Optional<RendezVous> findNextAppointmentByDoctor(@Param("medecinUserId") Long medecinUserId,
                                                     @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * Trouve les rendez-vous d'un médecin pour une date spécifique (toute la journée).
     *
     * @param medecinUserId l'ID du médecin
     * @param startOfDay début de la journée
     * @param endOfDay fin de la journée
     * @return Liste des rendez-vous
     */
    @Query("SELECT r FROM RendezVous r WHERE r.medecinUserId = :medecinUserId " +
            "AND r.dateHeure >= :startOfDay " +
            "AND r.dateHeure <= :endOfDay " +
            "ORDER BY r.dateHeure ASC")
    List<RendezVous> findByMedecinUserIdAndDateRange(@Param("medecinUserId") Long medecinUserId,
                                                     @Param("startOfDay") LocalDateTime startOfDay,
                                                     @Param("endOfDay") LocalDateTime endOfDay);

    // ========================================
    // CONFLICT DETECTION QUERIES (FIXED)
    // ========================================

    /**
     * ✅ FIXED: Vérifie s'il existe un conflit de rendez-vous pour un médecin sur une plage horaire donnée.
     * Utilise une requête native PostgreSQL pour une compatibilité maximale.
     * Ne prend en compte que les rendez-vous dont le statut n'est pas ANNULE.
     *
     * @param medecinId l'ID du médecin
     * @param startDateTime début de la plage
     * @param endDateTime fin de la plage
     * @return true s'il y a conflit, false sinon
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM rendez_vous r " +
            "WHERE r.doctor_user_id = :medecinId " +
            "AND r.statut != 'ANNULE' " +
            "AND NOT (r.date_heure >= :endDateTime OR " +
            "         r.date_heure + (r.duree_minutes || ' minutes')::interval <= :startDateTime)",
            nativeQuery = true)
    boolean existsConflictingRdvMedecin(
            @Param("medecinId") Long medecinId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * ✅ FIXED: Vérifie s'il existe un conflit de rendez-vous pour un patient sur une plage horaire donnée.
     * Utilise une requête native PostgreSQL pour une compatibilité maximale.
     * Ne prend en compte que les rendez-vous dont le statut n'est pas ANNULE.
     *
     * @param patientId l'ID du patient
     * @param startDateTime début de la plage
     * @param endDateTime fin de la plage
     * @return true s'il y a conflit, false sinon
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM rendez_vous r " +
            "WHERE r.patient_user_id = :patientId " +
            "AND r.statut != 'ANNULE' " +
            "AND NOT (r.date_heure >= :endDateTime OR " +
            "         r.date_heure + (r.duree_minutes || ' minutes')::interval <= :startDateTime)",
            nativeQuery = true)
    boolean existsConflictingRdvPatient(
            @Param("patientId") Long patientId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * ✅ FIXED: Vérifie s'il existe un conflit de rendez-vous pour un médecin, en excluant un RDV spécifique.
     *
     * @param medecinId l'ID du médecin
     * @param startDateTime début de la plage
     * @param endDateTime fin de la plage
     * @param excludeId ID du rendez-vous à exclure
     * @return true s'il y a conflit, false sinon
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM rendez_vous r " +
            "WHERE r.doctor_user_id = :medecinId " +
            "AND r.id != :excludeId " +
            "AND r.statut != 'ANNULE' " +
            "AND NOT (r.date_heure >= :endDateTime OR " +
            "         r.date_heure + (r.duree_minutes || ' minutes')::interval <= :startDateTime)",
            nativeQuery = true)
    boolean existsConflictingRdvMedecinExcluding(
            @Param("medecinId") Long medecinId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("excludeId") Long excludeId
    );

    /**
     * ✅ FIXED: Vérifie s'il existe un conflit de rendez-vous pour un patient, en excluant un RDV spécifique.
     *
     * @param patientId l'ID du patient
     * @param startDateTime début de la plage
     * @param endDateTime fin de la plage
     * @param excludeId ID du rendez-vous à exclure
     * @return true s'il y a conflit, false sinon
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END " +
            "FROM rendez_vous r " +
            "WHERE r.patient_user_id = :patientId " +
            "AND r.id != :excludeId " +
            "AND r.statut != 'ANNULE' " +
            "AND NOT (r.date_heure >= :endDateTime OR " +
            "         r.date_heure + (r.duree_minutes || ' minutes')::interval <= :startDateTime)",
            nativeQuery = true)
    boolean existsConflictingRdvPatientExcluding(
            @Param("patientId") Long patientId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("excludeId") Long excludeId
    );

    // ========================================
    // STATISTICS QUERIES
    // ========================================

    /**
     * Compte le nombre de rendez-vous par statut.
     *
     * @param statut le statut
     * @return nombre de rendez-vous
     */
    Long countByStatut(RendezVousStatut statut);

    /**
     * Compte le nombre de rendez-vous d'un médecin par statut.
     *
     * @param medecinUserId l'ID du médecin
     * @param statut le statut
     * @return nombre de rendez-vous
     */
    @Query("SELECT COUNT(r) FROM RendezVous r WHERE r.medecinUserId = :medecinUserId AND r.statut = :statut")
    Long countByMedecinUserIdAndStatut(@Param("medecinUserId") Long medecinUserId, @Param("statut") RendezVousStatut statut);

    /**
     * Compte le nombre de rendez-vous dans une période donnée.
     *
     * @param startDate début période
     * @param endDate fin période
     * @return nombre de rendez-vous
     */
    @Query("SELECT COUNT(r) FROM RendezVous r WHERE r.dateHeure >= :startDate AND r.dateHeure <= :endDate")
    Long countByDateHeureBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Compte le nombre de rendez-vous d'un médecin dans une période donnée.
     *
     * @param medecinUserId l'ID du médecin
     * @param startDate début période
     * @param endDate fin période
     * @return nombre de rendez-vous
     */
    @Query("SELECT COUNT(r) FROM RendezVous r WHERE r.medecinUserId = :medecinUserId " +
            "AND r.dateHeure >= :startDate AND r.dateHeure <= :endDate")
    Long countByMedecinUserIdAndDateHeureBetween(@Param("medecinUserId") Long medecinUserId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Trouve les médecins les plus occupés (par nombre de rendez-vous).
     *
     * @return Liste des IDs de médecins avec leur nombre de rendez-vous
     */
    @Query("SELECT r.medecinUserId, COUNT(r) as appointmentCount " +
            "FROM RendezVous r " +
            "WHERE r.statut != 'ANNULE' " +
            "GROUP BY r.medecinUserId " +
            "ORDER BY appointmentCount DESC")
    List<Object[]> findBusiestDoctors();

    /**
     * Trouve les créneaux horaires les plus demandés.
     *
     * @return Liste des heures avec leur nombre de rendez-vous
     */
    @Query("SELECT HOUR(r.dateHeure) as hour, COUNT(r) as appointmentCount " +
            "FROM RendezVous r " +
            "WHERE r.statut != 'ANNULE' " +
            "GROUP BY HOUR(r.dateHeure) " +
            "ORDER BY appointmentCount DESC")
    List<Object[]> findMostPopularTimeSlots();

    /**
     * Trouve les jours de la semaine les plus demandés.
     *
     * @return Liste des jours avec leur nombre de rendez-vous
     */
    @Query("SELECT FUNCTION('DAYOFWEEK', r.dateHeure) as dayOfWeek, COUNT(r) as appointmentCount " +
            "FROM RendezVous r " +
            "WHERE r.statut != 'ANNULE' " +
            "GROUP BY FUNCTION('DAYOFWEEK', r.dateHeure) " +
            "ORDER BY appointmentCount DESC")
    List<Object[]> findMostPopularDaysOfWeek();

    /**
     * Statistiques globales des rendez-vous.
     *
     * @return Array contenant [total, confirmés, annulés, terminés]
     */
    @Query("SELECT " +
            "COUNT(r), " +
            "SUM(CASE WHEN r.statut = 'CONFIRME' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.statut = 'ANNULE' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.statut = 'TERMINE' THEN 1 ELSE 0 END) " +
            "FROM RendezVous r")
    Object[] getGlobalStatistics();

    /**
     * Statistiques des rendez-vous pour un médecin.
     *
     * @param medecinUserId l'ID du médecin
     * @return Array contenant [total, confirmés, annulés, terminés]
     */
    @Query("SELECT " +
            "COUNT(r), " +
            "SUM(CASE WHEN r.statut = 'CONFIRME' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.statut = 'ANNULE' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN r.statut = 'TERMINE' THEN 1 ELSE 0 END) " +
            "FROM RendezVous r WHERE r.medecinUserId = :medecinUserId")
    Object[] getDoctorStatistics(@Param("medecinUserId") Long medecinUserId);

    // ========================================
    // DELETE QUERIES
    // ========================================

    /**
     * Supprime les rendez-vous annulés plus anciens qu'une date donnée.
     *
     * @param cutoffDate date limite
     * @return nombre de rendez-vous supprimés
     */
    @Query("DELETE FROM RendezVous r WHERE r.statut = 'ANNULE' AND r.dateHeure < :cutoffDate")
    int deleteOldCancelledAppointments(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Supprime les rendez-vous terminés plus anciens qu'une date donnée.
     *
     * @param cutoffDate date limite
     * @return nombre de rendez-vous supprimés
     */
    @Query("DELETE FROM RendezVous r WHERE r.statut = 'TERMINE' AND r.dateHeure < :cutoffDate")
    int deleteOldCompletedAppointments(@Param("cutoffDate") LocalDateTime cutoffDate);
}
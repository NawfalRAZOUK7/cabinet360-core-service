package com.cabinet360.core.repository;

import com.cabinet360.core.entity.RendezVous;
import com.cabinet360.core.enums.RendezVousStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des rendez-vous médicaux.
 */
@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    /**
     * Recherche tous les rendez-vous d’un patient donné.
     *
     * @param patientUserId l’ID du patient (auth-service)
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByPatientUserId(Long patientUserId);

    /**
     * Recherche tous les rendez-vous d’un médecin donné.
     *
     * @param medecinUserId l’ID du médecin (auth-service)
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByMedecinUserId(Long medecinUserId);

    /**
     * Recherche les rendez-vous d’un médecin à une date donnée.
     *
     * @param medecinUserId l’ID du médecin
     * @param dateHeure la date recherchée
     * @return Liste des rendez-vous correspondants
     */
    List<RendezVous> findByMedecinUserIdAndDateHeure(Long medecinUserId, LocalDateTime dateHeure);

    /**
     * Recherche tous les rendez-vous d’un patient sur une période donnée.
     *
     * @param patientUserId l’ID du patient
     * @param startDate début période
     * @param endDate fin période
     * @return Liste des rendez-vous
     */
    List<RendezVous> findByPatientUserIdAndDateHeureBetween(Long patientUserId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Recherche tous les rendez-vous par statut (ex : SCHEDULED, CANCELED, etc.)
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

    /**
     * Vérifie s’il existe un conflit de rendez-vous pour un médecin sur une plage horaire donnée.
     * Utilise une requête native avec tsrange pour détecter les chevauchements.
     * Ne prend en compte que les rendez-vous dont le statut n’est pas CANCELED.
     *
     * @param medecinId l’ID du médecin
     * @param startDateTime début de la plage
     * @param endDateTime fin de la plage
     * @return true s’il y a conflit, false sinon
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM rendez_vous r
        WHERE r.doctor_user_id = :medecinId
          AND r.statut != 'CANCELED'
          AND tsrange(r.date_heure, r.date_heure + (r.duree_minutes || ' minutes')::interval, '[)') &&
              tsrange(:startDateTime, :endDateTime, '[)')
        """, nativeQuery = true)
    boolean existsConflictingRdvMedecin(
            @Param("medecinId") Long medecinId, // ID du médecin concerné
            @Param("startDateTime") LocalDateTime startDateTime, // Début du créneau à vérifier
            @Param("endDateTime") LocalDateTime endDateTime      // Fin du créneau à vérifier
    );

    /**
     * Vérifie s’il existe un conflit de rendez-vous pour un patient sur une plage horaire donnée.
     * Utilise une requête native avec tsrange pour détecter les chevauchements.
     * Ne prend en compte que les rendez-vous dont le statut n’est pas CANCELED.
     *
     * @param patientId l’ID du patient
     * @param startDateTime début de la plage
     * @param endDateTime fin de la plage
     * @return true s’il y a conflit, false sinon
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM rendez_vous r
        WHERE r.patient_user_id = :patientId
          AND r.statut != 'CANCELED'
          AND tsrange(r.date_heure, r.date_heure + (r.duree_minutes || ' minutes')::interval, '[)') &&
              tsrange(:startDateTime, :endDateTime, '[)')
        """, nativeQuery = true)
    boolean existsConflictingRdvPatient(
            @Param("patientId") Long patientId, // ID du patient concerné
            @Param("startDateTime") LocalDateTime startDateTime, // Début du créneau à vérifier
            @Param("endDateTime") LocalDateTime endDateTime      // Fin du créneau à vérifier
    );
}

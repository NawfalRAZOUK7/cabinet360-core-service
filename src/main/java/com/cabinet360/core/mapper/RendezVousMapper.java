package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.RendezVousDto;
import com.cabinet360.core.entity.RendezVous;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre les entités RendezVous et les DTOs RendezVousDto.
 * Gère la conversion bidirectionnelle avec tous les champs et optimisations pour les listes.
 */
public class RendezVousMapper {

    // ========================================
    // BASIC CONVERSION METHODS
    // ========================================

    /**
     * Convertit une entité RendezVous en DTO RendezVousDto (conversion complète).
     *
     * @param entity l'entité à convertir
     * @return le DTO correspondant, ou null si l'entité est null
     */
    public static RendezVousDto toDto(RendezVous entity) {
        if (entity == null) {
            return null;
        }

        RendezVousDto dto = new RendezVousDto(
                entity.getId(),
                entity.getPatientUserId(),
                entity.getMedecinUserId(),
                entity.getDateHeure(),
                entity.getStatut(),
                entity.getDureeMinutes(),
                entity.getMotif(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );

        // Les champs calculés sont automatiquement mis à jour dans le constructeur du DTO
        return dto;
    }

    /**
     * Convertit un DTO RendezVousDto en entité RendezVous.
     *
     * @param dto le DTO à convertir
     * @return l'entité correspondante, ou null si le DTO est null
     */
    public static RendezVous toEntity(RendezVousDto dto) {
        if (dto == null) {
            return null;
        }

        RendezVous entity = new RendezVous(
                dto.getId(),
                dto.getPatientUserId(),
                dto.getMedecinUserId(),
                dto.getDateHeure(),
                dto.getStatut(),
                dto.getDureeMinutes()
        );

        // Ajout des champs optionnels
        entity.setMotif(dto.getMotif());
        entity.setNotes(dto.getNotes());

        // Les timestamps sont gérés par les annotations JPA, mais on peut les définir si fournis
        if (dto.getCreatedAt() != null) {
            entity.setCreatedAt(dto.getCreatedAt());
        }
        if (dto.getUpdatedAt() != null) {
            entity.setUpdatedAt(dto.getUpdatedAt());
        }

        return entity;
    }

    // ========================================
    // BULK CONVERSION METHODS
    // ========================================

    /**
     * Convertit une liste d'entités en liste de DTOs.
     *
     * @param entities la liste d'entités à convertir
     * @return la liste de DTOs correspondante
     */
    public static List<RendezVousDto> toDtoList(List<RendezVous> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(RendezVousMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste de DTOs en liste d'entités.
     *
     * @param dtos la liste de DTOs à convertir
     * @return la liste d'entités correspondante
     */
    public static List<RendezVous> toEntityList(List<RendezVousDto> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(RendezVousMapper::toEntity)
                .collect(Collectors.toList());
    }

    // ========================================
    // OPTIMIZED CONVERSION METHODS
    // ========================================

    /**
     * Convertit une entité en DTO "light" (sans tous les détails).
     * Utile pour les listes où on n'a pas besoin de tous les champs.
     * Optimise les performances pour les grandes listes.
     *
     * @param entity l'entité à convertir
     * @return un DTO avec les champs essentiels seulement
     */
    public static RendezVousDto toLightDto(RendezVous entity) {
        if (entity == null) {
            return null;
        }

        RendezVousDto dto = new RendezVousDto();
        dto.setId(entity.getId());
        dto.setPatientUserId(entity.getPatientUserId());
        dto.setMedecinUserId(entity.getMedecinUserId());
        dto.setDateHeure(entity.getDateHeure());
        dto.setStatut(entity.getStatut());
        dto.setDureeMinutes(entity.getDureeMinutes());
        // On ne copie pas motif, notes, timestamps pour alléger

        return dto;
    }

    /**
     * Convertit une liste d'entités en liste de DTOs "light".
     * Optimisé pour les grandes listes (dashboards, statistiques, etc.).
     *
     * @param entities la liste d'entités à convertir
     * @return la liste de DTOs light correspondante
     */
    public static List<RendezVousDto> toLightDtoList(List<RendezVous> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(RendezVousMapper::toLightDto)
                .collect(Collectors.toList());
    }

    // ========================================
    // UPDATE METHODS
    // ========================================

    /**
     * Met à jour une entité existante avec les données d'un DTO.
     * Utile pour les opérations de mise à jour partielle.
     * Ne modifie que les champs non-null du DTO.
     *
     * @param entity l'entité à mettre à jour
     * @param dto le DTO contenant les nouvelles données
     * @return l'entité mise à jour
     */
    public static RendezVous updateEntityFromDto(RendezVous entity, RendezVousDto dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        // Mise à jour des champs principaux (seulement si non-null)
        if (dto.getPatientUserId() != null) {
            entity.setPatientUserId(dto.getPatientUserId());
        }
        if (dto.getMedecinUserId() != null) {
            entity.setMedecinUserId(dto.getMedecinUserId());
        }
        if (dto.getDateHeure() != null) {
            entity.setDateHeure(dto.getDateHeure());
        }
        if (dto.getStatut() != null) {
            entity.setStatut(dto.getStatut());
        }
        if (dto.getDureeMinutes() != null) {
            entity.setDureeMinutes(dto.getDureeMinutes());
        }

        // Mise à jour des champs optionnels (permet l'effacement avec une chaîne vide)
        if (dto.getMotif() != null) {
            entity.setMotif(dto.getMotif().trim().isEmpty() ? null : dto.getMotif());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes().trim().isEmpty() ? null : dto.getNotes());
        }

        return entity;
    }

    /**
     * Met à jour une entité avec TOUS les champs du DTO (même les null).
     * Utilisé quand on veut forcer une mise à jour complète.
     *
     * @param entity l'entité à mettre à jour
     * @param dto le DTO contenant les nouvelles données
     * @return l'entité mise à jour
     */
    public static RendezVous forceUpdateEntityFromDto(RendezVous entity, RendezVousDto dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        // Mise à jour forcée de tous les champs
        entity.setPatientUserId(dto.getPatientUserId());
        entity.setMedecinUserId(dto.getMedecinUserId());
        entity.setDateHeure(dto.getDateHeure());
        entity.setStatut(dto.getStatut());
        entity.setDureeMinutes(dto.getDureeMinutes());
        entity.setMotif(dto.getMotif());
        entity.setNotes(dto.getNotes());

        return entity;
    }

    // ========================================
    // COPY METHODS
    // ========================================

    /**
     * Copie les champs d'un DTO vers un autre DTO.
     * Utile pour fusionner des données ou créer des copies.
     *
     * @param source le DTO source
     * @param target le DTO cible
     * @return le DTO cible mis à jour
     */
    public static RendezVousDto copyDtoFields(RendezVousDto source, RendezVousDto target) {
        if (source == null || target == null) {
            return target;
        }

        if (source.getId() != null) {
            target.setId(source.getId());
        }
        if (source.getPatientUserId() != null) {
            target.setPatientUserId(source.getPatientUserId());
        }
        if (source.getMedecinUserId() != null) {
            target.setMedecinUserId(source.getMedecinUserId());
        }
        if (source.getDateHeure() != null) {
            target.setDateHeure(source.getDateHeure());
        }
        if (source.getStatut() != null) {
            target.setStatut(source.getStatut());
        }
        if (source.getDureeMinutes() != null) {
            target.setDureeMinutes(source.getDureeMinutes());
        }
        if (source.getMotif() != null) {
            target.setMotif(source.getMotif());
        }
        if (source.getNotes() != null) {
            target.setNotes(source.getNotes());
        }

        return target;
    }

    /**
     * Crée une copie complète d'un DTO.
     *
     * @param source le DTO à copier
     * @return une nouvelle instance avec les mêmes données
     */
    public static RendezVousDto cloneDto(RendezVousDto source) {
        if (source == null) {
            return null;
        }

        return new RendezVousDto(
                source.getId(),
                source.getPatientUserId(),
                source.getMedecinUserId(),
                source.getDateHeure(),
                source.getStatut(),
                source.getDureeMinutes(),
                source.getMotif(),
                source.getNotes(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }

    // ========================================
    // FACTORY METHODS
    // ========================================

    /**
     * Crée un DTO à partir des paramètres essentiels seulement.
     * Utile pour les créations rapides depuis l'API.
     *
     * @param patientUserId ID du patient
     * @param medecinUserId ID du médecin
     * @param dateHeure date et heure du rendez-vous
     * @param dureeMinutes durée en minutes
     * @return un nouveau DTO
     */
    public static RendezVousDto createDto(Long patientUserId, Long medecinUserId,
                                          java.time.LocalDateTime dateHeure, Integer dureeMinutes) {
        RendezVousDto dto = new RendezVousDto();
        dto.setPatientUserId(patientUserId);
        dto.setMedecinUserId(medecinUserId);
        dto.setDateHeure(dateHeure);
        dto.setDureeMinutes(dureeMinutes != null ? dureeMinutes : 30);
        dto.setStatut(com.cabinet360.core.enums.RendezVousStatut.CONFIRME);
        return dto;
    }

    /**
     * Crée un DTO pour un nouveau rendez-vous avec motif.
     *
     * @param patientUserId ID du patient
     * @param medecinUserId ID du médecin
     * @param dateHeure date et heure du rendez-vous
     * @param dureeMinutes durée en minutes
     * @param motif motif du rendez-vous
     * @return un nouveau DTO
     */
    public static RendezVousDto createDtoWithMotif(Long patientUserId, Long medecinUserId,
                                                   java.time.LocalDateTime dateHeure, Integer dureeMinutes,
                                                   String motif) {
        RendezVousDto dto = createDto(patientUserId, medecinUserId, dateHeure, dureeMinutes);
        dto.setMotif(motif);
        return dto;
    }

    /**
     * Crée un DTO complet pour un nouveau rendez-vous.
     *
     * @param patientUserId ID du patient
     * @param medecinUserId ID du médecin
     * @param dateHeure date et heure du rendez-vous
     * @param dureeMinutes durée en minutes
     * @param motif motif du rendez-vous
     * @param notes notes additionnelles
     * @return un nouveau DTO complet
     */
    public static RendezVousDto createFullDto(Long patientUserId, Long medecinUserId,
                                              java.time.LocalDateTime dateHeure, Integer dureeMinutes,
                                              String motif, String notes) {
        RendezVousDto dto = createDtoWithMotif(patientUserId, medecinUserId, dateHeure, dureeMinutes, motif);
        dto.setNotes(notes);
        return dto;
    }

    // ========================================
    // VALIDATION METHODS
    // ========================================

    /**
     * Valide qu'un DTO contient les champs obligatoires pour la création.
     *
     * @param dto le DTO à valider
     * @return true si valide, false sinon
     */
    public static boolean isValidForCreation(RendezVousDto dto) {
        if (dto == null) {
            return false;
        }

        return dto.getPatientUserId() != null &&
                dto.getMedecinUserId() != null &&
                dto.getDateHeure() != null &&
                dto.getDureeMinutes() != null &&
                dto.getDureeMinutes() > 0;
    }

    /**
     * Valide qu'un DTO contient les champs obligatoires pour la mise à jour.
     *
     * @param dto le DTO à valider
     * @return true si valide, false sinon
     */
    public static boolean isValidForUpdate(RendezVousDto dto) {
        if (dto == null || dto.getId() == null) {
            return false;
        }

        // Pour une mise à jour, au moins un champ modifiable doit être présent
        return dto.getDateHeure() != null ||
                dto.getStatut() != null ||
                dto.getDureeMinutes() != null ||
                dto.getMotif() != null ||
                dto.getNotes() != null;
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Compare deux DTOs pour détecter les changements.
     *
     * @param original le DTO original
     * @param updated le DTO mis à jour
     * @return true si des changements sont détectés, false sinon
     */
    public static boolean hasChanges(RendezVousDto original, RendezVousDto updated) {
        if (original == null && updated == null) {
            return false;
        }
        if (original == null || updated == null) {
            return true;
        }

        return !java.util.Objects.equals(original.getPatientUserId(), updated.getPatientUserId()) ||
                !java.util.Objects.equals(original.getMedecinUserId(), updated.getMedecinUserId()) ||
                !java.util.Objects.equals(original.getDateHeure(), updated.getDateHeure()) ||
                !java.util.Objects.equals(original.getStatut(), updated.getStatut()) ||
                !java.util.Objects.equals(original.getDureeMinutes(), updated.getDureeMinutes()) ||
                !java.util.Objects.equals(original.getMotif(), updated.getMotif()) ||
                !java.util.Objects.equals(original.getNotes(), updated.getNotes());
    }

    /**
     * Extrait les champs modifiés entre deux DTOs.
     *
     * @param original le DTO original
     * @param updated le DTO mis à jour
     * @return une liste des noms des champs modifiés
     */
    public static java.util.List<String> getModifiedFields(RendezVousDto original, RendezVousDto updated) {
        java.util.List<String> modifiedFields = new java.util.ArrayList<>();

        if (original == null || updated == null) {
            return modifiedFields;
        }

        if (!java.util.Objects.equals(original.getPatientUserId(), updated.getPatientUserId())) {
            modifiedFields.add("patientUserId");
        }
        if (!java.util.Objects.equals(original.getMedecinUserId(), updated.getMedecinUserId())) {
            modifiedFields.add("medecinUserId");
        }
        if (!java.util.Objects.equals(original.getDateHeure(), updated.getDateHeure())) {
            modifiedFields.add("dateHeure");
        }
        if (!java.util.Objects.equals(original.getStatut(), updated.getStatut())) {
            modifiedFields.add("statut");
        }
        if (!java.util.Objects.equals(original.getDureeMinutes(), updated.getDureeMinutes())) {
            modifiedFields.add("dureeMinutes");
        }
        if (!java.util.Objects.equals(original.getMotif(), updated.getMotif())) {
            modifiedFields.add("motif");
        }
        if (!java.util.Objects.equals(original.getNotes(), updated.getNotes())) {
            modifiedFields.add("notes");
        }

        return modifiedFields;
    }

    /**
     * Nettoie les chaînes de caractères d'un DTO (trim, null si vide).
     *
     * @param dto le DTO à nettoyer
     * @return le DTO nettoyé
     */
    public static RendezVousDto sanitizeDto(RendezVousDto dto) {
        if (dto == null) {
            return null;
        }

        if (dto.getMotif() != null) {
            String motif = dto.getMotif().trim();
            dto.setMotif(motif.isEmpty() ? null : motif);
        }

        if (dto.getNotes() != null) {
            String notes = dto.getNotes().trim();
            dto.setNotes(notes.isEmpty() ? null : notes);
        }

        return dto;
    }

    // ========================================
    // SPECIALIZED CONVERSION METHODS
    // ========================================

    /**
     * Convertit une entité en DTO pour l'affichage dans un calendrier.
     * Inclut les champs calculés nécessaires pour l'affichage calendrier.
     *
     * @param entity l'entité à convertir
     * @return un DTO optimisé pour l'affichage calendrier
     */
    public static RendezVousDto toCalendarDto(RendezVous entity) {
        if (entity == null) {
            return null;
        }

        RendezVousDto dto = toLightDto(entity);
        // Ajouter le motif pour l'affichage dans le calendrier
        dto.setMotif(entity.getMotif());

        return dto;
    }

    /**
     * Convertit une liste d'entités en DTOs pour l'affichage dans un calendrier.
     *
     * @param entities la liste d'entités
     * @return la liste de DTOs pour calendrier
     */
    public static List<RendezVousDto> toCalendarDtoList(List<RendezVous> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(RendezVousMapper::toCalendarDto)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une entité en DTO pour les statistiques.
     * Exclut les données sensibles comme les notes.
     *
     * @param entity l'entité à convertir
     * @return un DTO pour statistiques
     */
    public static RendezVousDto toStatsDto(RendezVous entity) {
        if (entity == null) {
            return null;
        }

        RendezVousDto dto = new RendezVousDto();
        dto.setId(entity.getId());
        dto.setMedecinUserId(entity.getMedecinUserId());
        dto.setDateHeure(entity.getDateHeure());
        dto.setStatut(entity.getStatut());
        dto.setDureeMinutes(entity.getDureeMinutes());
        dto.setCreatedAt(entity.getCreatedAt());
        // Pas de notes, motif, ou patientUserId pour les stats anonymisées

        return dto;
    }
}
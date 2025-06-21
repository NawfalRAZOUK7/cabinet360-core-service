package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.RendezVousDto;
import com.cabinet360.core.entity.RendezVous;

public class RendezVousMapper {

    public static RendezVousDto toDto(RendezVous entity) {
        if (entity == null) return null;
        return new RendezVousDto(
                entity.getId(),
                entity.getPatientUserId(),
                entity.getMedecinUserId(),
                entity.getDateHeure(),
                entity.getStatut(),
                entity.getDureeMinutes()
        );
    }

    public static RendezVous toEntity(RendezVousDto dto) {
        if (dto == null) return null;
        return new RendezVous(
                dto.getId(),
                dto.getPatientUserId(),
                dto.getMedecinUserId(),
                dto.getDateHeure(),
                dto.getStatut(),
                dto.getDureeMinutes()
        );
    }
}

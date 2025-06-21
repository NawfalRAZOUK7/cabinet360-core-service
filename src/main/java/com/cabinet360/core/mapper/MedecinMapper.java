package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.MedecinDto;
import com.cabinet360.core.entity.Medecin;

public class MedecinMapper {

    public static MedecinDto toDto(Medecin entity) {
        if (entity == null) return null;

        // Comme l'entité core-service ne contient pas specialite ni disponibilite,
        // ces champs seront remplis via la couche client REST (auth-service)
        return new MedecinDto(
                entity.getDoctorUserId(),
                null, // specialite non stockée ici
                null  // isAvailable non stocké ici
        );
    }

    public static Medecin toEntity(MedecinDto dto) {
        if (dto == null) return null;
        return new Medecin(
                null, // id auto généré
                dto.getDoctorUserId()
        );
    }
}

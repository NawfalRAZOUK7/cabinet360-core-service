package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.PatientDto;
import com.cabinet360.core.entity.Patient;

public class PatientMapper {

    public static PatientDto toDto(Patient entity) {
        if (entity == null) return null;
        // Ici, nom, prenom et actif doivent venir d’une autre source (auth-service),
        // donc dans core-service ils restent null par défaut
        return new PatientDto(
                entity.getPatientUserId(),
                null,
                null,
                null
        );
    }

    public static Patient toEntity(PatientDto dto) {
        if (dto == null) return null;
        return new Patient(
                null,
                dto.getPatientUserId()
        );
    }
}

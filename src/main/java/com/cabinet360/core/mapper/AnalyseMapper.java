package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.AnalyseDto;
import com.cabinet360.core.entity.Analyse;
import com.cabinet360.core.entity.DossierMedical;
import org.mapstruct.*;

/**
 * ✅ FIXED: MapStruct mapper for Analyse entity/DTO conversion
 * This will generate the implementation automatically and be registered as a Spring bean
 */
@Mapper(
        componentModel = "spring",  // ✅ CRITICAL: This makes it a Spring bean
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AnalyseMapper {

    /**
     * ✅ Convert entity to DTO
     */
    @Mapping(target = "dossierMedicalId", source = "dossierMedical.id")
    AnalyseDto toDto(Analyse entity);

    /**
     * ✅ Convert DTO to entity
     */
    @Mapping(target = "dossierMedical", expression = "java(createDossierReference(dto.getDossierMedicalId()))")
    @Mapping(target = "id", ignore = true) // Let database generate ID for new entities
    Analyse toEntity(AnalyseDto dto);

    /**
     * ✅ Update existing entity from DTO (for PUT operations)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Never update ID
    @Mapping(target = "dossierMedical", expression = "java(updateDossierReference(dto.getDossierMedicalId(), entity.getDossierMedical()))")
    void updateEntityFromDto(AnalyseDto dto, @MappingTarget Analyse entity);

    /**
     * ✅ Helper method to create DossierMedical reference for new entities
     */
    default DossierMedical createDossierReference(Long dossierMedicalId) {
        if (dossierMedicalId == null) {
            return null;
        }
        DossierMedical dossier = new DossierMedical();
        dossier.setId(dossierMedicalId);
        return dossier;
    }

    /**
     * ✅ Helper method to update DossierMedical reference (only if ID changed)
     */
    default DossierMedical updateDossierReference(Long newDossierId, DossierMedical currentDossier) {
        if (newDossierId == null) {
            return currentDossier; // Keep current if no new ID provided
        }

        // If same ID, keep current reference
        if (currentDossier != null && newDossierId.equals(currentDossier.getId())) {
            return currentDossier;
        }

        // Create new reference if ID changed
        DossierMedical newDossier = new DossierMedical();
        newDossier.setId(newDossierId);
        return newDossier;
    }
}
package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.OrdonnanceDto;
import com.cabinet360.core.entity.Ordonnance;
import com.cabinet360.core.entity.DossierMedical;
import org.mapstruct.*;

/**
 * ✅ Fixed: Corrected mapper with proper field mappings
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrdonnanceMapper {

    /**
     * ✅ Fixed: Direct field mapping, no custom mapping needed
     */
    @Mapping(target = "dossierMedicalId", source = "dossierMedical.id")
    OrdonnanceDto toDto(Ordonnance entity);

    /**
     * ✅ Fixed: Proper entity creation
     */
    @Mapping(target = "dossierMedical", expression = "java(createDossierMedicalReference(dto.getDossierMedicalId()))")
    Ordonnance toEntity(OrdonnanceDto dto);

    /**
     * ✅ Fixed: Update method with proper field mapping
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dossierMedical", expression = "java(createDossierMedicalReference(dto.getDossierMedicalId()))")
    void updateEntityFromDto(OrdonnanceDto dto, @MappingTarget Ordonnance entity);

    /**
     * ✅ Fixed: Proper reference creation (will be injected via service)
     */
    default DossierMedical createDossierMedicalReference(Long dossierMedicalId) {
        if (dossierMedicalId == null) return null;
        DossierMedical dm = new DossierMedical();
        dm.setId(dossierMedicalId);
        return dm;
    }
}
package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.NoteMedicaleDto;
import com.cabinet360.core.entity.NoteMedicale;
import com.cabinet360.core.entity.DossierMedical;
import org.mapstruct.*;

/**
 * ✅ Fixed: Corrected mapper with proper field mappings
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoteMedicaleMapper {

    /**
     * ✅ Fixed: Direct field mapping, no custom mapping needed
     */
    @Mapping(target = "dossierMedicalId", source = "dossierMedical.id")
    NoteMedicaleDto toDto(NoteMedicale entity);

    /**
     * ✅ Fixed: Proper entity creation
     */
    @Mapping(target = "dossierMedical", expression = "java(createDossierMedicalReference(dto.getDossierMedicalId()))")
    NoteMedicale toEntity(NoteMedicaleDto dto);

    /**
     * ✅ Fixed: Update method with proper field mapping
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dossierMedical", expression = "java(createDossierMedicalReference(dto.getDossierMedicalId()))")
    void updateEntityFromDto(NoteMedicaleDto dto, @MappingTarget NoteMedicale entity);

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
package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.DossierMedicalDto;
import com.cabinet360.core.entity.*;
import com.cabinet360.core.repository.MedecinRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ Corrected MapStruct mapper for DossierMedical
 */
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class DossierMedicalMapper {

    @Autowired
    protected MedecinRepository medecinRepository;

    /**
     * ✅ Complete mapping from entity to DTO
     */
    @Mappings({
            @Mapping(target = "analyseIds", source = "analyses", qualifiedByName = "mapAnalyseIds"),
            @Mapping(target = "ordonnanceIds", source = "ordonnances", qualifiedByName = "mapOrdonnanceIds"),
            @Mapping(target = "documentIds", source = "documents", qualifiedByName = "mapDocumentIds"),
            @Mapping(target = "noteIds", source = "notes", qualifiedByName = "mapNoteIds"),
            @Mapping(target = "medecinsAutorisesIds", source = "medecinsAutorises", qualifiedByName = "mapMedecinUserIds")
    })
    public abstract DossierMedicalDto toDto(DossierMedical entity);

    /**
     * ✅ Mapping from DTO to entity (excludes complex relationships)
     */
    @Mappings({
            @Mapping(target = "analyses", ignore = true),
            @Mapping(target = "ordonnances", ignore = true),
            @Mapping(target = "documents", ignore = true),
            @Mapping(target = "notes", ignore = true),
            @Mapping(target = "medecinsAutorises", ignore = true)
    })
    public abstract DossierMedical toEntity(DossierMedicalDto dto);

    /**
     * ✅ Update entity from DTO (for partial updates)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "patientUserId", ignore = true), // Never change patient
            @Mapping(target = "createdAt", ignore = true), // Never change creation date
            @Mapping(target = "analyses", ignore = true),
            @Mapping(target = "ordonnances", ignore = true),
            @Mapping(target = "documents", ignore = true),
            @Mapping(target = "notes", ignore = true),
            @Mapping(target = "medecinsAutorises", ignore = true)
    })
    public abstract void updateEntityFromDto(DossierMedicalDto dto, @MappingTarget DossierMedical entity);

    // ========== Custom mapping methods ==========

    @Named("mapAnalyseIds")
    protected List<Long> mapAnalyseIds(List<Analyse> analyses) {
        if (analyses == null) return null;
        return analyses.stream()
                .map(Analyse::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    @Named("mapOrdonnanceIds")
    protected List<Long> mapOrdonnanceIds(List<Ordonnance> ordonnances) {
        if (ordonnances == null) return null;
        return ordonnances.stream()
                .map(Ordonnance::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    @Named("mapDocumentIds")
    protected List<Long> mapDocumentIds(List<Document> documents) {
        if (documents == null) return null;
        return documents.stream()
                .map(Document::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    @Named("mapNoteIds")
    protected List<Long> mapNoteIds(List<NoteMedicale> notes) {
        if (notes == null) return null;
        return notes.stream()
                .map(NoteMedicale::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    @Named("mapMedecinUserIds")
    protected List<Long> mapMedecinUserIds(List<Medecin> medecins) {
        if (medecins == null) return null;
        return medecins.stream()
                .map(Medecin::getDoctorUserId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Helper method to resolve medecins from IDs (used in service layer)
     */
    public List<Medecin> resolveMedecinsFromIds(List<Long> medecinUserIds) {
        if (medecinUserIds == null || medecinUserIds.isEmpty()) {
            return null;
        }

        return medecinUserIds.stream()
                .map(doctorUserId -> medecinRepository.findByDoctorUserId(doctorUserId).orElse(null))
                .filter(medecin -> medecin != null)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Helper to map list of entities to DTOs
     */
    public List<DossierMedicalDto> toDtoList(List<DossierMedical> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Helper to map list of DTOs to entities
     */
    public List<DossierMedical> toEntityList(List<DossierMedicalDto> dtos) {
        if (dtos == null) return null;
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
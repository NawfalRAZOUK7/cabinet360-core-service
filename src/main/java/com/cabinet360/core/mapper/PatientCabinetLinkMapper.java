package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.PatientCabinetLinkDto;
import com.cabinet360.core.entity.PatientCabinetLink;

public class PatientCabinetLinkMapper {

    public static PatientCabinetLinkDto toDto(PatientCabinetLink entity) {
        if (entity == null) return null;

        return PatientCabinetLinkDto.builder()
                .id(entity.getId())
                .patientUserId(entity.getPatientUserId())
                .cabinetId(entity.getCabinetId())
                .status(entity.getStatus())
                .linkedAt(entity.getLinkedAt())
                .lastAccessAt(entity.getLastAccessAt())
                .linkNotes(entity.getLinkNotes())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    public static PatientCabinetLink toEntity(PatientCabinetLinkDto dto) {
        if (dto == null) return null;

        PatientCabinetLink entity = new PatientCabinetLink();
        entity.setId(dto.getId());
        entity.setPatientUserId(dto.getPatientUserId());
        entity.setCabinetId(dto.getCabinetId());
        entity.setStatus(dto.getStatus());
        entity.setLinkedAt(dto.getLinkedAt());
        entity.setLastAccessAt(dto.getLastAccessAt());
        entity.setLinkNotes(dto.getLinkNotes());
        entity.setDeletedAt(dto.getDeletedAt());
        return entity;
    }
}
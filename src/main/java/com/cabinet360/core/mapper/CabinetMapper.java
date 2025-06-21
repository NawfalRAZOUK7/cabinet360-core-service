package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.CabinetDto;
import com.cabinet360.core.dto.CabinetSettingsDto;
import com.cabinet360.core.entity.Cabinet;
import com.cabinet360.core.entity.CabinetSettings;

public class CabinetMapper {

    // === Entity → DTO ===
    public static CabinetDto toDto(Cabinet entity) {
        if (entity == null) return null;

        return CabinetDto.builder()
                .id(entity.getId())
                .ownerDoctorId(entity.getOwnerDoctorId())
                .cabinetName(entity.getCabinetName())
                .description(entity.getDescription())
                .businessLicense(entity.getBusinessLicense())
                .status(entity.getStatus())
                .settings(toSettingsDto(entity.getSettings()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    // === DTO → Entity ===
    public static Cabinet toEntity(CabinetDto dto) {
        if (dto == null) return null;

        return Cabinet.builder()
                .ownerDoctorId(dto.getOwnerDoctorId())
                .cabinetName(dto.getCabinetName())
                .description(dto.getDescription())
                .businessLicense(dto.getBusinessLicense())
                .status(dto.getStatus())
                .settings(toSettingsEntity(dto.getSettings()))
                .build();
    }

    // === Settings Entity → DTO ===
    public static CabinetSettingsDto toSettingsDto(CabinetSettings entity) {
        if (entity == null) return null;

        return CabinetSettingsDto.builder()
                .defaultAppointmentDuration(entity.getDefaultAppointmentDuration())
                .workingDays(entity.getWorkingDays())
                .workingHours(entity.getWorkingHoursStart(), entity.getWorkingHoursEnd())
                .allowOnlineBooking(entity.getAllowOnlineBooking())
                .maxPatientsPerDay(entity.getMaxPatientsPerDay())
                .advanceBookingDays(entity.getAdvanceBookingDays())
                .requirePatientConfirmation(entity.getRequirePatientConfirmation())
                .reminders(entity.getSendReminders(), entity.getReminderHoursBefore())
                .emergencyContact(entity.getEmergencyContact())
                .build();
    }

    // === Settings DTO → Entity ===
    public static CabinetSettings toSettingsEntity(CabinetSettingsDto dto) {
        if (dto == null) return new CabinetSettings();

        return CabinetSettings.builder()
                .defaultAppointmentDuration(dto.getDefaultAppointmentDuration())
                .workingDays(dto.getWorkingDays())
                .workingHours(dto.getWorkingHoursStart(), dto.getWorkingHoursEnd())
                .allowOnlineBooking(dto.getAllowOnlineBooking())
                .maxPatientsPerDay(dto.getMaxPatientsPerDay())
                .advanceBookingDays(dto.getAdvanceBookingDays())
                .requirePatientConfirmation(dto.getRequirePatientConfirmation())
                .reminders(dto.getSendReminders(), dto.getReminderHoursBefore())
                .emergencyContact(dto.getEmergencyContact())
                .build();
    }
}
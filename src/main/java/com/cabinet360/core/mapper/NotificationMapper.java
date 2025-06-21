package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.*;
import com.cabinet360.core.entity.Notification;
import com.cabinet360.core.enums.NotificationType;

public class NotificationMapper {

    // ✅ COMPLETELY REWRITTEN: Entity → DTO
    public static NotificationDto toDto(Notification entity) {
        if (entity == null) return null;

        switch (entity.getType()) {
            case EMAIL:
                return EmailNotificationDto.builder()
                        .id(entity.getId())
                        .recipient(entity.getRecipient())
                        .message(entity.getMessage())
                        .subject(extractSubjectFromMessage(entity.getMessage()))
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .sentAt(entity.getSentAt())
                        .errorMessage(entity.getErrorMessage())
                        .build();

            case SMS:
                return SmsNotificationDto.builder()
                        .id(entity.getId())
                        .recipient(entity.getRecipient())
                        .message(entity.getMessage())
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .sentAt(entity.getSentAt())
                        .errorMessage(entity.getErrorMessage())
                        .build();

            case PUSH:
                return PushNotificationDto.builder()
                        .id(entity.getId())
                        .recipient(entity.getRecipient())
                        .message(entity.getMessage())
                        .title(extractTitleFromMessage(entity.getMessage()))
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .sentAt(entity.getSentAt())
                        .errorMessage(entity.getErrorMessage())
                        .dataPayload(null)
                        .build();

            case IN_APP:
                return AppNotificationDto.builder()
                        .id(entity.getId())
                        .recipient(entity.getRecipient())
                        .message(entity.getMessage())
                        .title(extractTitleFromMessage(entity.getMessage()))
                        .status(entity.getStatus())
                        .createdAt(entity.getCreatedAt())
                        .sentAt(entity.getSentAt())
                        .errorMessage(entity.getErrorMessage())
                        .build();

            default:
                throw new IllegalArgumentException("Unsupported notification type: " + entity.getType());
        }
    }

    // ✅ COMPLETELY REWRITTEN: DTO → Entity
    public static Notification toEntity(NotificationDto dto) {
        if (dto == null) return null;

        return Notification.builder()
                .type(dto.getType())
                .recipient(dto.getRecipient())
                .message(dto.getMessage())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .sentAt(dto.getSentAt())
                .errorMessage(dto.getErrorMessage())
                .build();
    }

    // ✅ Helper method to extract subject from message (you can customize this)
    private static String extractSubjectFromMessage(String message) {
        if (message == null || message.length() <= 50) {
            return message;
        }
        return message.substring(0, 47) + "...";
    }

    // ✅ Helper method to extract title from message (you can customize this)
    private static String extractTitleFromMessage(String message) {
        if (message == null || message.length() <= 30) {
            return message;
        }
        return message.substring(0, 27) + "...";
    }
}
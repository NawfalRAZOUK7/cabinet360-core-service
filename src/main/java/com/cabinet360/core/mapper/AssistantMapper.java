package com.cabinet360.core.mapper;

import com.cabinet360.core.dto.AssistantDto;
import com.cabinet360.core.entity.Assistant;

public class AssistantMapper {

    public static AssistantDto toDto(Assistant entity) {
        if (entity == null) return null;
        // Champs métiers enrichis non stockés en core-service
        return new AssistantDto(
                entity.getAssistantUserId(),
                null,
                null,
                null
        );
    }

    public static Assistant toEntity(AssistantDto dto) {
        if (dto == null) return null;
        Assistant entity = new Assistant();
        entity.setAssistantUserId(dto.getAssistantUserId());
        return entity;
    }
}

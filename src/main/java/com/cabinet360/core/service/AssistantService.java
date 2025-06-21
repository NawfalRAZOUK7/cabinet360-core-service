package com.cabinet360.core.service;

import com.cabinet360.core.client.AuthServiceClient;
import com.cabinet360.core.dto.AssistantDto;
import com.cabinet360.core.entity.Assistant;
import com.cabinet360.core.mapper.AssistantMapper;
import com.cabinet360.core.repository.AssistantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssistantService {

    @Autowired
    private AssistantRepository assistantRepository;

    @Autowired
    private AuthServiceClient authServiceClient;

    // CRUD local sur entité légère Assistant
    public AssistantDto createAssistant(AssistantDto dto) {
        Assistant assistant = AssistantMapper.toEntity(dto);
        Assistant saved = assistantRepository.save(assistant);
        return AssistantMapper.toDto(saved);
    }

    public AssistantDto updateAssistant(Long id, AssistantDto dto) {
        Assistant existing = assistantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assistant non trouvé"));
        existing.setAssistantUserId(dto.getAssistantUserId());
        Assistant updated = assistantRepository.save(existing);
        return AssistantMapper.toDto(updated);
    }

    public void deleteAssistant(Long id) {
        if (!assistantRepository.existsById(id)) {
            throw new IllegalArgumentException("Assistant non trouvé");
        }
        assistantRepository.deleteById(id);
    }

    public List<AssistantDto> listAllAssistants() {
        return assistantRepository.findAll().stream()
                .map(AssistantMapper::toDto)
                .toList();
    }

    // Recherche enrichie via auth-service
    public List<AssistantDto> getAssistantsFiltered(String nom, Boolean actif) {
        return authServiceClient.getAssistantsByFilter(nom, actif);
    }
}

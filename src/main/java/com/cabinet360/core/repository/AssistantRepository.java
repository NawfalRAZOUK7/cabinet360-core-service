// AssistantRepository.java
package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Assistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssistantRepository extends JpaRepository<Assistant, Long> {
    Optional<Assistant> findByAssistantUserId(Long assistantUserId);
}

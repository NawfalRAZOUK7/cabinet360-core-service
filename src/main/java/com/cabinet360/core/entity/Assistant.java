package com.cabinet360.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "assistants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assistant_user", columnNames = {"assistant_user_id"})
        },
        indexes = {
                @Index(name = "idx_assistant_user", columnList = "assistant_user_id")
        }
)
public class Assistant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "assistantUserId is required")
    @Column(name = "assistant_user_id", nullable = false, unique = true)
    private Long assistantUserId;

    // --- Constructors ---
    public Assistant() {}

    public Assistant(Long id, Long assistantUserId) {
        this.id = id;
        this.assistantUserId = assistantUserId;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssistantUserId() { return assistantUserId; }
    public void setAssistantUserId(Long assistantUserId) { this.assistantUserId = assistantUserId; }

    @Override
    public String toString() {
        return "Assistant{" +
                "id=" + id +
                ", assistantUserId=" + assistantUserId +
                '}';
    }
}

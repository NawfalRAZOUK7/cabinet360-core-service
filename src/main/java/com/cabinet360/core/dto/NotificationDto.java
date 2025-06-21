package com.cabinet360.core.dto;

import com.cabinet360.core.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class NotificationDto {

    private Long id;

    private NotificationType type;

    @NotBlank(message = "Recipient cannot be blank")
    @Size(max = 255, message = "Recipient must be less than 255 characters")
    private String recipient;

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 1000, message = "Message must be less than 1000 characters")
    private String message;

    @Size(max = 50, message = "Status must be less than 50 characters")
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt; // ✅ ADDED: Missing field
    private String errorMessage;  // ✅ ADDED: Missing field

    // ---- Constructors ----
    protected NotificationDto() {}

    protected NotificationDto(Long id, NotificationType type, String recipient, String message,
                              String status, LocalDateTime createdAt, LocalDateTime sentAt,
                              String errorMessage) {
        this.id = id;
        this.type = type;
        this.recipient = recipient;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
    }

    // --- Builder Pattern ---
    protected static abstract class Builder<T extends NotificationDto, B extends Builder<T, B>> {
        protected Long id;
        protected NotificationType type;
        protected String recipient;
        protected String message;
        protected String status;
        protected LocalDateTime createdAt;
        protected LocalDateTime sentAt;
        protected String errorMessage;

        protected abstract B self();
        protected abstract T build();

        public B id(Long id) {
            this.id = id;
            return self();
        }

        public B type(NotificationType type) {
            this.type = type;
            return self();
        }

        public B recipient(String recipient) {
            this.recipient = recipient;
            return self();
        }

        public B message(String message) {
            this.message = message;
            return self();
        }

        public B status(String status) {
            this.status = status;
            return self();
        }

        public B createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return self();
        }

        public B sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return self();
        }

        public B errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return self();
        }
    }

    // ---- Getters & Setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() { // ✅ ADDED: New getter
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) { // ✅ ADDED: New setter
        this.sentAt = sentAt;
    }

    public String getErrorMessage() { // ✅ ADDED: New getter
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) { // ✅ ADDED: New setter
        this.errorMessage = errorMessage;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationDto that = (NotificationDto) o;
        return Objects.equals(id, that.id) &&
                type == that.type &&
                Objects.equals(recipient, that.recipient) &&
                Objects.equals(message, that.message) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, recipient, message, status);
    }

    @Override
    public String toString() {
        return "NotificationDto{" +
                "id=" + id +
                ", type=" + type +
                ", recipient='" + recipient + '\'' +
                ", message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", sentAt=" + sentAt +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
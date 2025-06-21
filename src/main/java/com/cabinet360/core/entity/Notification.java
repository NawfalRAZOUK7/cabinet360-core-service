package com.cabinet360.core.entity;

import com.cabinet360.core.enums.NotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_type", columnList = "type"),
                @Index(name = "idx_notification_recipient", columnList = "recipient"),
                @Index(name = "idx_notification_status", columnList = "status"),
                @Index(name = "idx_notification_created_at", columnList = "createdAt")
        }
)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Type of notification, e.g. EMAIL, SMS, PUSH, IN_APP
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    /**
     * Recipient address or identifier: email, phone number, deviceToken, or user ID
     */
    @Column(nullable = false, length = 255)
    private String recipient;

    /**
     * Content/message body of the notification
     */
    @Column(nullable = false, length = 1000)
    private String message; // ✅ FIXED: Changed from 'content' to 'message'

    /**
     * Current status of the notification: PENDING, SENT, FAILED
     */
    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    /**
     * When the notification was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * When the notification was sent (nullable if not sent yet)
     */
    @Column
    private LocalDateTime sentAt;

    /**
     * Error message if sending failed (nullable)
     */
    @Column(length = 1000)
    private String errorMessage;

    // --- Constructors ---

    public Notification() {
        // JPA requires a default constructor
    }

    public Notification(NotificationType type, String recipient, String message) {
        this.type = type;
        this.recipient = recipient;
        this.message = message; // ✅ FIXED: Updated field name
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    public Notification(NotificationType type, String recipient, String message, String status,
                        LocalDateTime createdAt, LocalDateTime sentAt, String errorMessage) {
        this.type = type;
        this.recipient = recipient;
        this.message = message; // ✅ FIXED: Updated field name
        this.status = status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
    }

    // --- Builder Pattern ---
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private NotificationType type;
        private String recipient;
        private String message;
        private String status = "PENDING";
        private LocalDateTime createdAt;
        private LocalDateTime sentAt;
        private String errorMessage;

        public Builder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Notification build() {
            Notification notification = new Notification();
            notification.type = this.type;
            notification.recipient = this.recipient;
            notification.message = this.message;
            notification.status = this.status;
            notification.createdAt = this.createdAt != null ? this.createdAt : LocalDateTime.now();
            notification.sentAt = this.sentAt;
            notification.errorMessage = this.errorMessage;
            return notification;
        }
    }

    // --- Getters and Setters ---

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

    public String getMessage() { // ✅ FIXED: Updated method name
        return message;
    }

    public void setMessage(String message) { // ✅ FIXED: Updated method name
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
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
        return "Notification{" +
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
package com.cabinet360.core.dto;

import com.cabinet360.core.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.cabinet360.core.enums.NotificationType.EMAIL;

public class EmailNotificationDto extends NotificationDto {

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject;

    // --- Constructors ---
    public EmailNotificationDto() {
        super();
    }

    // ✅ FIXED: Updated constructor
    public EmailNotificationDto(Long id, String emailAddress, String message, String subject,
                                String status, LocalDateTime createdAt, LocalDateTime sentAt,
                                String errorMessage) {
        super(id, EMAIL, emailAddress, message, status, createdAt, sentAt, errorMessage);
        this.subject = subject;
    }

    // Simple constructor for quick creation
    public EmailNotificationDto(String emailAddress, String subject, String message) {
        super(null, EMAIL, emailAddress, message, "PENDING", null, null, null);
        this.subject = subject;
    }

    // --- Builder Pattern ---
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends NotificationDto.Builder<EmailNotificationDto, Builder> {
        private String subject;

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public EmailNotificationDto build() {
            EmailNotificationDto dto = new EmailNotificationDto();
            dto.setId(this.id);
            dto.setType(EMAIL);
            dto.setRecipient(this.recipient);
            dto.setMessage(this.message);
            dto.setStatus(this.status);
            dto.setCreatedAt(this.createdAt);
            dto.setSentAt(this.sentAt);
            dto.setErrorMessage(this.errorMessage);
            dto.setSubject(this.subject);
            return dto;
        }
    }

    // --- Getters & Setters ---
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EmailNotificationDto that = (EmailNotificationDto) o;
        return Objects.equals(subject, that.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subject);
    }

    @Override
    public String toString() {
        return "EmailNotificationDto{" +
                "subject='" + subject + '\'' +
                ", " + super.toString() +
                '}';
    }
}
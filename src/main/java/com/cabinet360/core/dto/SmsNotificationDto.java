package com.cabinet360.core.dto;

import com.cabinet360.core.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.cabinet360.core.enums.NotificationType.SMS;

public class SmsNotificationDto extends NotificationDto {

    // --- Constructors ---
    public SmsNotificationDto() {
        super();
    }

    // ✅ FIXED: Updated constructor
    public SmsNotificationDto(Long id, String phoneNumber, String message, String status,
                              LocalDateTime createdAt, LocalDateTime sentAt, String errorMessage) {
        super(id, SMS, phoneNumber, message, status, createdAt, sentAt, errorMessage);
    }

    // Simple constructor for quick creation
    public SmsNotificationDto(String phoneNumber, String message) {
        super(null, SMS, phoneNumber, message, "PENDING", null, null, null);
    }

    // --- Builder Pattern ---
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends NotificationDto.Builder<SmsNotificationDto, Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public SmsNotificationDto build() {
            SmsNotificationDto dto = new SmsNotificationDto();
            dto.setId(this.id);
            dto.setType(SMS);
            dto.setRecipient(this.recipient);
            dto.setMessage(this.message);
            dto.setStatus(this.status);
            dto.setCreatedAt(this.createdAt);
            dto.setSentAt(this.sentAt);
            dto.setErrorMessage(this.errorMessage);
            return dto;
        }
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "SmsNotificationDto{" + super.toString() + '}';
    }
}
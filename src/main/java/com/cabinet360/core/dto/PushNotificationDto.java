package com.cabinet360.core.dto;

import com.cabinet360.core.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.cabinet360.core.enums.NotificationType.PUSH;

public class PushNotificationDto extends NotificationDto {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Size(max = 2000, message = "Payload must be less than 2000 characters")
    private String dataPayload;

    // --- Constructors ---
    public PushNotificationDto() {
        super();
    }

    // ✅ FIXED: Updated constructor
    public PushNotificationDto(Long id, String deviceToken, String title, String message,
                               String status, LocalDateTime createdAt, LocalDateTime sentAt,
                               String errorMessage, String dataPayload) {
        super(id, PUSH, deviceToken, message, status, createdAt, sentAt, errorMessage);
        this.title = title;
        this.dataPayload = dataPayload;
    }

    // Simple constructor for quick creation
    public PushNotificationDto(String deviceToken, String title, String message, String dataPayload) {
        super(null, PUSH, deviceToken, message, "PENDING", null, null, null);
        this.title = title;
        this.dataPayload = dataPayload;
    }

    // --- Builder Pattern ---
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends NotificationDto.Builder<PushNotificationDto, Builder> {
        private String title;
        private String dataPayload;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder dataPayload(String dataPayload) {
            this.dataPayload = dataPayload;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public PushNotificationDto build() {
            PushNotificationDto dto = new PushNotificationDto();
            dto.setId(this.id);
            dto.setType(PUSH);
            dto.setRecipient(this.recipient);
            dto.setMessage(this.message);
            dto.setStatus(this.status);
            dto.setCreatedAt(this.createdAt);
            dto.setSentAt(this.sentAt);
            dto.setErrorMessage(this.errorMessage);
            dto.setTitle(this.title);
            dto.setDataPayload(this.dataPayload);
            return dto;
        }
    }

    // --- Getters & Setters ---
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDataPayload() {
        return dataPayload;
    }

    public void setDataPayload(String dataPayload) {
        this.dataPayload = dataPayload;
    }

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PushNotificationDto that = (PushNotificationDto) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(dataPayload, that.dataPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title, dataPayload);
    }

    @Override
    public String toString() {
        return "PushNotificationDto{" +
                "title='" + title + '\'' +
                ", dataPayload='" + dataPayload + '\'' +
                ", " + super.toString() +
                '}';
    }
}
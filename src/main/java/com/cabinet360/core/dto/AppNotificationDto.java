package com.cabinet360.core.dto;

import com.cabinet360.core.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Objects;

import static com.cabinet360.core.enums.NotificationType.IN_APP;

public class AppNotificationDto extends NotificationDto {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    // --- Constructors ---
    public AppNotificationDto() {
        super();
    }

    // ✅ FIXED: Updated constructor
    public AppNotificationDto(Long id, String userId, String title, String message,
                              String status, LocalDateTime createdAt, LocalDateTime sentAt,
                              String errorMessage) {
        super(id, IN_APP, userId, message, status, createdAt, sentAt, errorMessage);
        this.title = title;
    }

    // Simple constructor for quick creation
    public AppNotificationDto(String userId, String title, String message) {
        super(null, IN_APP, userId, message, "UNREAD", null, null, null);
        this.title = title;
    }

    // --- Builder Pattern ---
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends NotificationDto.Builder<AppNotificationDto, Builder> {
        private String title;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public AppNotificationDto build() {
            AppNotificationDto dto = new AppNotificationDto();
            dto.setId(this.id);
            dto.setType(IN_APP);
            dto.setRecipient(this.recipient);
            dto.setMessage(this.message);
            dto.setStatus(this.status);
            dto.setCreatedAt(this.createdAt);
            dto.setSentAt(this.sentAt);
            dto.setErrorMessage(this.errorMessage);
            dto.setTitle(this.title);
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

    // --- equals, hashCode, toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AppNotificationDto that = (AppNotificationDto) o;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), title);
    }

    @Override
    public String toString() {
        return "AppNotificationDto{" +
                "title='" + title + '\'' +
                ", " + super.toString() +
                '}';
    }
}
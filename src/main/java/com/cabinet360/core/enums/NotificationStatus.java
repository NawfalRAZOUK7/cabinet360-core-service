package com.cabinet360.core.enums;

public enum NotificationStatus {
    PENDING("PENDING", "Notification is queued for sending"),
    SENT("SENT", "Notification has been sent successfully"),
    FAILED("FAILED", "Notification sending failed"),
    UNREAD("UNREAD", "In-app notification is unread"),
    READ("READ", "In-app notification has been read"),
    CANCELLED("CANCELLED", "Notification was cancelled before sending");

    private final String value;
    private final String description;

    NotificationStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return value;
    }

    public static NotificationStatus fromValue(String value) {
        for (NotificationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown notification status: " + value);
    }
}
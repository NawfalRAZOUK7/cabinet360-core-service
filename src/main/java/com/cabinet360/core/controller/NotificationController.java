package com.cabinet360.core.controller;

import com.cabinet360.core.dto.*;
import com.cabinet360.core.enums.NotificationStatus;
import com.cabinet360.core.enums.NotificationType;
import com.cabinet360.core.service.NotificationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for Notification Management
 * Handles all notification operations including sending, tracking, and management
 */
@RestController
@RequestMapping("/api/v1/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ========================================
    // SEND NOTIFICATIONS
    // ========================================

    /**
     * Send a notification asynchronously
     * POST /api/v1/notifications/send
     */
    @PostMapping("/send")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendNotification(@Valid @RequestBody NotificationDto notificationDto) {
        logger.info("📢 Sending {} notification to: {}",
                notificationDto.getType(), notificationDto.getRecipient());

        try {
            CompletableFuture<Boolean> future = notificationService.sendNotification(notificationDto);

            // For async processing, return immediately with accepted status
            return ResponseEntity.accepted().body(Map.of(
                    "status", "accepted",
                    "message", "Notification queued for processing",
                    "type", notificationDto.getType().toString(),
                    "recipient", notificationDto.getRecipient()
            ));

        } catch (Exception e) {
            logger.error("❌ Failed to queue notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to queue notification: " + e.getMessage()
            ));
        }
    }

    /**
     * Send a notification synchronously (for urgent notifications)
     * POST /api/v1/notifications/send/immediate
     */
    @PostMapping("/send/immediate")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendImmediateNotification(@Valid @RequestBody NotificationDto notificationDto) {
        logger.info("⚡ Sending immediate {} notification to: {}",
                notificationDto.getType(), notificationDto.getRecipient());

        try {
            boolean success = notificationService.sendImmediateNotification(notificationDto);

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Notification sent successfully",
                        "type", notificationDto.getType().toString(),
                        "recipient", notificationDto.getRecipient()
                ));
            } else {
                return ResponseEntity.unprocessableEntity().body(Map.of(
                        "status", "failed",
                        "message", "Failed to send notification"
                ));
            }

        } catch (Exception e) {
            logger.error("❌ Failed to send immediate notification: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Internal error: " + e.getMessage()
            ));
        }
    }

    /**
     * Send batch notifications
     * POST /api/v1/notifications/send/batch
     */
    @PostMapping("/send/batch")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendBatchNotifications(@Valid @RequestBody List<NotificationDto> notifications) {
        logger.info("📦 Sending batch of {} notifications", notifications.size());

        try {
            CompletableFuture<Integer> future = notificationService.sendBatchNotifications(notifications);

            return ResponseEntity.accepted().body(Map.of(
                    "status", "accepted",
                    "message", "Batch notifications queued for processing",
                    "totalCount", notifications.size()
            ));

        } catch (Exception e) {
            logger.error("❌ Failed to queue batch notifications: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to queue batch: " + e.getMessage()
            ));
        }
    }

    /**
     * Send notification using simple parameters
     * POST /api/v1/notifications/send/simple
     */
    @PostMapping("/send/simple")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT')")
    public ResponseEntity<Map<String, Object>> sendSimpleNotification(
            @RequestParam NotificationType type,
            @RequestParam String recipient,
            @RequestParam String message,
            @RequestParam(required = false) String additionalInfo) {

        logger.info("📤 Sending simple {} notification to: {}", type, recipient);

        try {
            CompletableFuture<Boolean> future = notificationService.createAndSendNotification(
                    type, recipient, message, additionalInfo);

            return ResponseEntity.accepted().body(Map.of(
                    "status", "accepted",
                    "message", "Simple notification queued",
                    "type", type.toString(),
                    "recipient", recipient
            ));

        } catch (Exception e) {
            logger.error("❌ Failed to send simple notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    // ========================================
    // READ OPERATIONS
    // ========================================

    /**
     * Get notification by ID
     * GET /api/v1/notifications/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        logger.info("🔍 Fetching notification: {}", id);

        Optional<NotificationDto> notification = notificationService.findById(id);
        return notification.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all notifications
     * GET /api/v1/notifications
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getAllNotifications() {
        logger.info("🔍 Fetching all notifications");

        List<NotificationDto> notifications = notificationService.findAll();
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by recipient
     * GET /api/v1/notifications/recipient/{recipient}
     */
    @GetMapping("/recipient/{recipient}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (@authUser.email == #recipient and hasRole('PATIENT'))")
    public ResponseEntity<List<NotificationDto>> getNotificationsByRecipient(@PathVariable String recipient) {
        logger.info("🔍 Fetching notifications for recipient: {}", recipient);

        List<NotificationDto> notifications = notificationService.findByRecipient(recipient);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by type
     * GET /api/v1/notifications/type/{type}
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getNotificationsByType(@PathVariable NotificationType type) {
        logger.info("🔍 Fetching notifications by type: {}", type);

        List<NotificationDto> notifications = notificationService.findByType(type);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications by status
     * GET /api/v1/notifications/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getNotificationsByStatus(@PathVariable String status) {
        logger.info("🔍 Fetching notifications by status: {}", status);

        List<NotificationDto> notifications = notificationService.findByStatus(status);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get recent notifications for a recipient
     * GET /api/v1/notifications/recipient/{recipient}/recent?days=7
     */
    @GetMapping("/recipient/{recipient}/recent")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (@authUser.email == #recipient and hasRole('PATIENT'))")
    public ResponseEntity<List<NotificationDto>> getRecentNotifications(
            @PathVariable String recipient,
            @RequestParam(defaultValue = "7") int days) {

        logger.info("🔍 Fetching recent notifications for {} (last {} days)", recipient, days);

        List<NotificationDto> notifications = notificationService.getRecentNotifications(recipient, days);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get in-app notifications for a user
     * GET /api/v1/notifications/user/{userId}/in-app
     */
    @GetMapping("/user/{userId}/in-app")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (@authUser.id == #userId and hasRole('PATIENT'))")
    public ResponseEntity<List<NotificationDto>> getInAppNotifications(@PathVariable String userId) {
        logger.info("🔍 Fetching in-app notifications for user: {}", userId);

        List<NotificationDto> notifications = notificationService.findAppNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    // ========================================
    // STATUS MANAGEMENT
    // ========================================

    /**
     * Update notification status
     * PATCH /api/v1/notifications/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateNotificationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        logger.info("🔄 Updating notification {} status", id);

        try {
            String statusStr = request.get("status");
            NotificationStatus status = NotificationStatus.valueOf(statusStr.toUpperCase());

            boolean updated = notificationService.updateStatus(id, status);

            if (updated) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Status updated successfully",
                        "notificationId", id,
                        "newStatus", status.toString()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid status value"
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to update status: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Mark notification as read
     * PATCH /api/v1/notifications/{id}/read
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        logger.info("👁️ Marking notification {} as read", id);

        boolean updated = notificationService.markAsRead(id);

        if (updated) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Notification marked as read",
                    "notificationId", id
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mark notification as unread
     * PATCH /api/v1/notifications/{id}/unread
     */
    @PatchMapping("/{id}/unread")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('PATIENT')")
    public ResponseEntity<Map<String, Object>> markAsUnread(@PathVariable Long id) {
        logger.info("👁️ Marking notification {} as unread", id);

        boolean updated = notificationService.markAsUnread(id);

        if (updated) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Notification marked as unread",
                    "notificationId", id
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // ANALYTICS & MONITORING
    // ========================================

    /**
     * Get unread count for a user
     * GET /api/v1/notifications/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or (@authUser.id == #userId and hasRole('PATIENT'))")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String userId) {
        logger.info("📊 Getting unread count for user: {}", userId);

        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Get failed notification count for a recipient
     * GET /api/v1/notifications/recipient/{recipient}/failed-count
     */
    @GetMapping("/recipient/{recipient}/failed-count")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('ASSISTANT') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getFailedCount(@PathVariable String recipient) {
        logger.info("📊 Getting failed count for recipient: {}", recipient);

        long count = notificationService.getFailedCount(recipient);
        return ResponseEntity.ok(Map.of("failedCount", count));
    }

    // ========================================
    // MAINTENANCE OPERATIONS
    // ========================================

    /**
     * Retry failed notifications
     * POST /api/v1/notifications/retry-failed
     */
    @PostMapping("/retry-failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> retryFailedNotifications() {
        logger.info("🔄 Retrying failed notifications");

        try {
            notificationService.retryFailedNotifications();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Failed notifications retry initiated"
            ));
        } catch (Exception e) {
            logger.error("❌ Failed to retry notifications: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Cleanup old notifications
     * DELETE /api/v1/notifications/cleanup?olderThanDays=30
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupOldNotifications(
            @RequestParam(defaultValue = "30") int olderThanDays) {

        logger.info("🧹 Cleaning up notifications older than {} days", olderThanDays);

        try {
            int deletedCount = notificationService.cleanupOldNotifications(olderThanDays);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Cleanup completed",
                    "deletedCount", deletedCount,
                    "olderThanDays", olderThanDays
            ));
        } catch (Exception e) {
            logger.error("❌ Cleanup failed: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Delete notification by ID
     * DELETE /api/v1/notifications/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        logger.info("🗑️ Deleting notification: {}", id);

        boolean deleted = notificationService.deleteById(id);

        if (deleted) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Notification deleted successfully",
                    "notificationId", id.toString()
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // UTILITY ENDPOINTS
    // ========================================

    /**
     * Get available notification types
     * GET /api/v1/notifications/types
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getNotificationTypes() {
        return ResponseEntity.ok(Map.of(
                "types", NotificationType.values(),
                "count", NotificationType.values().length
        ));
    }

    /**
     * Get available notification statuses
     * GET /api/v1/notifications/statuses
     */
    @GetMapping("/statuses")
    public ResponseEntity<Map<String, Object>> getNotificationStatuses() {
        return ResponseEntity.ok(Map.of(
                "statuses", NotificationStatus.values(),
                "count", NotificationStatus.values().length
        ));
    }

    /**
     * Health check endpoint for notification service
     * GET /api/v1/notifications/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "NotificationService",
                "timestamp", System.currentTimeMillis(),
                "supportedTypes", NotificationType.values().length
        ));
    }

    /**
     * Get notification service statistics
     * GET /api/v1/notifications/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        logger.info("📊 Fetching notification statistics");

        try {
            // Get basic counts by status
            long totalNotifications = notificationService.findAll().size();
            long pendingCount = notificationService.findByStatus("PENDING").size();
            long sentCount = notificationService.findByStatus("SENT").size();
            long failedCount = notificationService.findByStatus("FAILED").size();

            return ResponseEntity.ok(Map.of(
                    "totalNotifications", totalNotifications,
                    "pendingCount", pendingCount,
                    "sentCount", sentCount,
                    "failedCount", failedCount,
                    "successRate", totalNotifications > 0 ? (double) sentCount / totalNotifications * 100 : 0.0
            ));

        } catch (Exception e) {
            logger.error("❌ Failed to get stats: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
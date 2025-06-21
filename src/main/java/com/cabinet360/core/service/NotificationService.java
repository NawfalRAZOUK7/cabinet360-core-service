package com.cabinet360.core.service;

import com.cabinet360.core.dto.*;
import com.cabinet360.core.entity.Notification;
import com.cabinet360.core.enums.NotificationType;
import com.cabinet360.core.enums.NotificationStatus;
import com.cabinet360.core.mapper.NotificationMapper;
import com.cabinet360.core.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushService pushService;

    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService,
                               SmsService smsService,
                               PushService pushService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.pushService = pushService;
    }

    /**
     * Send and persist a notification (async + transactional)
     * Main orchestration method for all notification types
     */
    @Async
    @Transactional
    public CompletableFuture<Boolean> sendNotification(NotificationDto notificationDto) {
        logger.info("🔄 Processing notification: Type={}, Recipient={}",
                notificationDto.getType(), notificationDto.getRecipient());

        // Convert DTO to Entity and persist with PENDING status
        Notification notification = NotificationMapper.toEntity(notificationDto);
        notification.setStatus(NotificationStatus.PENDING.getValue());
        notification = notificationRepository.save(notification);

        try {
            boolean success = false;
            String errorMessage = null;

            // Delegate to appropriate service based on type
            switch (notificationDto.getType()) {
                case SMS -> {
                    if (notificationDto instanceof SmsNotificationDto smsDto) {
                        success = smsService.sendSms(smsDto);
                        if (!success) {
                            errorMessage = "SMS sending failed through Twilio service";
                        }
                    } else {
                        errorMessage = "Invalid DTO type for SMS notification";
                    }
                }
                case EMAIL -> {
                    if (notificationDto instanceof EmailNotificationDto emailDto) {
                        success = emailService.sendEmail(emailDto);
                        if (!success) {
                            errorMessage = "Email sending failed through SMTP service";
                        }
                    } else {
                        errorMessage = "Invalid DTO type for Email notification";
                    }
                }
                case PUSH -> {
                    if (notificationDto instanceof PushNotificationDto pushDto) {
                        success = pushService.sendPush(pushDto);
                        if (!success) {
                            errorMessage = "Push notification sending failed";
                        }
                    } else {
                        errorMessage = "Invalid DTO type for Push notification";
                    }
                }
                case IN_APP -> {
                    // For App notifications, saving in DB = successful delivery
                    success = true;
                    notification.setStatus(NotificationStatus.UNREAD.getValue());
                    logger.info("📱 In-app notification stored as UNREAD for user: {}",
                            notificationDto.getRecipient());
                }
                default -> {
                    errorMessage = "Unsupported notification type: " + notificationDto.getType();
                    logger.error("❌ {}", errorMessage);
                }
            }

            // Update notification status based on result
            updateNotificationStatus(notification, success, errorMessage);

            logger.info("✅ Notification processed: id={}, status={}, success={}",
                    notification.getId(), notification.getStatus(), success);

            return CompletableFuture.completedFuture(success);

        } catch (Exception e) {
            logger.error("💥 Failed to send notification: {}", e.getMessage(), e);
            updateNotificationStatus(notification, false, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    /**
     * Create and send notification using builder pattern - Convenience method
     */
    public CompletableFuture<Boolean> createAndSendNotification(NotificationType type,
                                                                String recipient,
                                                                String message,
                                                                String additionalInfo) {
        NotificationDto dto = buildNotificationDto(type, recipient, message, additionalInfo);
        return sendNotification(dto);
    }

    /**
     * Send immediate notification (synchronous) - For critical notifications
     */
    @Transactional
    public boolean sendImmediateNotification(NotificationDto notificationDto) {
        try {
            return sendNotification(notificationDto).get();
        } catch (Exception e) {
            logger.error("Failed to send immediate notification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Batch send notifications
     */
    @Async
    @Transactional
    public CompletableFuture<Integer> sendBatchNotifications(List<NotificationDto> notifications) {
        logger.info("📦 Processing batch of {} notifications", notifications.size());

        int successCount = 0;
        for (NotificationDto dto : notifications) {
            try {
                boolean success = sendNotification(dto).get();
                if (success) successCount++;
            } catch (Exception e) {
                logger.error("Failed to send notification in batch: {}", e.getMessage());
            }
        }

        logger.info("📊 Batch processing complete: {}/{} successful", successCount, notifications.size());
        return CompletableFuture.completedFuture(successCount);
    }

    // ===== CRUD Operations =====

    public Optional<NotificationDto> findById(Long id) {
        return notificationRepository.findById(id)
                .map(NotificationMapper::toDto);
    }

    public List<NotificationDto> findAll() {
        return notificationRepository.findAll().stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> findByRecipient(String recipient) {
        return notificationRepository.findByRecipient(recipient).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> findByType(NotificationType type) {
        return notificationRepository.findByType(type).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> findByStatus(String status) {
        return notificationRepository.findByStatus(status).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    // ===== Status Management =====

    @Transactional
    public boolean updateStatus(Long id, NotificationStatus newStatus) {
        Optional<Notification> optional = notificationRepository.findById(id);
        if (optional.isPresent()) {
            Notification notification = optional.get();
            notification.setStatus(newStatus.getValue());

            if (newStatus == NotificationStatus.SENT && notification.getSentAt() == null) {
                notification.setSentAt(LocalDateTime.now());
            }

            notificationRepository.save(notification);
            logger.info("🔄 Updated notification {} status to {}", id, newStatus);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean markAsRead(Long notificationId) {
        return updateStatus(notificationId, NotificationStatus.READ);
    }

    @Transactional
    public boolean markAsUnread(Long notificationId) {
        return updateStatus(notificationId, NotificationStatus.UNREAD);
    }

    // ===== Analytics & Monitoring =====

    public long getUnreadCount(String userId) {
        return notificationRepository.countByRecipientAndStatus(userId, NotificationStatus.UNREAD.getValue());
    }

    public long getFailedCount(String recipient) {
        return notificationRepository.countByRecipientAndStatus(recipient, NotificationStatus.FAILED.getValue());
    }

    public List<NotificationDto> getRecentNotifications(String recipient, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return notificationRepository.findRecentNotificationsByRecipient(recipient, since).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDto> findAppNotificationsByUserId(String userId) {
        return notificationRepository.findByRecipientAndType(userId, NotificationType.IN_APP).stream()
                .map(NotificationMapper::toDto)
                .collect(Collectors.toList());
    }

    // ===== Maintenance Operations =====

    @Transactional
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findByStatus(NotificationStatus.FAILED.getValue());
        logger.info("🔄 Retrying {} failed notifications", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            NotificationDto dto = NotificationMapper.toDto(notification);
            sendNotification(dto);
        }
    }

    @Transactional
    public int cleanupOldNotifications(int olderThanDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(olderThanDays);
        List<Notification> oldNotifications = notificationRepository.findByCreatedAtAfter(cutoff);

        List<Notification> toDelete = oldNotifications.stream()
                .filter(n -> NotificationStatus.SENT.getValue().equals(n.getStatus()) ||
                        NotificationStatus.READ.getValue().equals(n.getStatus()))
                .collect(Collectors.toList());

        notificationRepository.deleteAll(toDelete);
        logger.info("🧹 Cleaned up {} old notifications", toDelete.size());
        return toDelete.size();
    }

    @Transactional
    public boolean deleteById(Long id) {
        if (notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
            logger.info("🗑️ Deleted notification {}", id);
            return true;
        }
        return false;
    }

    // ===== Private Helper Methods =====

    private void updateNotificationStatus(Notification notification, boolean success, String errorMessage) {
        if (success && !NotificationStatus.UNREAD.getValue().equals(notification.getStatus())) {
            notification.setStatus(NotificationStatus.SENT.getValue());
            notification.setSentAt(LocalDateTime.now());
        } else if (!success) {
            notification.setStatus(NotificationStatus.FAILED.getValue());
            notification.setErrorMessage(errorMessage);
        }
        notificationRepository.save(notification);
    }

    private NotificationDto buildNotificationDto(NotificationType type, String recipient,
                                                 String message, String additionalInfo) {
        return switch (type) {
            case EMAIL -> EmailNotificationDto.builder()
                    .recipient(recipient)
                    .subject(additionalInfo != null ? additionalInfo : "Cabinet360 Notification")
                    .message(message)
                    .status(NotificationStatus.PENDING.getValue())
                    .build();

            case SMS -> SmsNotificationDto.builder()
                    .recipient(recipient)
                    .message(message)
                    .status(NotificationStatus.PENDING.getValue())
                    .build();

            case PUSH -> PushNotificationDto.builder()
                    .recipient(recipient)
                    .title(additionalInfo != null ? additionalInfo : "Cabinet360")
                    .message(message)
                    .status(NotificationStatus.PENDING.getValue())
                    .build();

            case IN_APP -> AppNotificationDto.builder()
                    .recipient(recipient)
                    .title(additionalInfo != null ? additionalInfo : "Notification")
                    .message(message)
                    .status(NotificationStatus.UNREAD.getValue())
                    .build();

            default -> throw new IllegalArgumentException("Unsupported notification type: " + type);
        };
    }

    @Override
    public String toString() {
        return "NotificationService{" +
                "repository=" + notificationRepository.getClass().getSimpleName() +
                ", emailService=" + emailService.getClass().getSimpleName() +
                ", smsService=" + smsService.getClass().getSimpleName() +
                ", pushService=" + pushService.getClass().getSimpleName() +
                '}';
    }
}
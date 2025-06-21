package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Notification;
import com.cabinet360.core.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find notifications by recipient
     */
    List<Notification> findByRecipient(String recipient);

    /**
     * ✅ FIXED: Find notifications by type (now uses enum)
     */
    List<Notification> findByType(NotificationType type);

    /**
     * Find notifications by status
     */
    List<Notification> findByStatus(String status);

    /**
     * Find notifications by recipient and type
     */
    List<Notification> findByRecipientAndType(String recipient, NotificationType type);

    /**
     * Find notifications by status and type
     */
    List<Notification> findByStatusAndType(String status, NotificationType type);

    /**
     * Find notifications created after a specific date
     */
    List<Notification> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find notifications that failed to send
     */
    List<Notification> findByStatusAndErrorMessageIsNotNull(String status);

    /**
     * Find pending notifications older than specified time
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.createdAt < :cutoffTime")
    List<Notification> findStaleNotifications(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count notifications by status for a specific recipient
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient = :recipient AND n.status = :status")
    long countByRecipientAndStatus(@Param("recipient") String recipient, @Param("status") String status);

    /**
     * Find recent notifications for a recipient (last N days)
     */
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByRecipient(
            @Param("recipient") String recipient,
            @Param("since") LocalDateTime since
    );
}
package com.cabinet360.core.service;

import com.cabinet360.core.dto.PushNotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // FCM Configuration
    @Value("${fcm.server.key:}")
    private String fcmServerKey;

    @Value("${fcm.url:https://fcm.googleapis.com/fcm/send}")
    private String fcmUrl;

    // OneSignal Configuration (Alternative)
    @Value("${onesignal.app.id:}")
    private String oneSignalAppId;

    @Value("${onesignal.api.key:}")
    private String oneSignalApiKey;

    @Value("${onesignal.url:https://onesignal.com/api/v1/notifications}")
    private String oneSignalUrl;

    // Service Configuration
    @Value("${push.service.provider:SIMULATION}")
    private String pushProvider; // FCM, ONESIGNAL, or SIMULATION

    @Value("${push.retry.attempts:3}")
    private int maxRetryAttempts;

    @Value("${push.test.mode:true}")
    private boolean testMode;

    @Value("${push.batch.size:1000}")
    private int batchSize;

    public PushService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send push notification - Main integration point with NotificationService
     */
    public boolean sendPush(PushNotificationDto pushDto) {
        try {
            logger.info("📲 Sending push notification to device: {}",
                    maskDeviceToken(pushDto.getRecipient()));

            if (testMode) {
                return simulatePushDelivery(pushDto);
            }

            return switch (pushProvider.toUpperCase()) {
                case "FCM" -> sendFcmPush(pushDto);
                case "ONESIGNAL" -> sendOneSignalPush(pushDto);
                default -> simulatePushDelivery(pushDto);
            };

        } catch (Exception e) {
            logger.error("💥 Failed to send push notification: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send push notification via Firebase Cloud Messaging (FCM)
     */
    private boolean sendFcmPush(PushNotificationDto pushDto) {
        if (fcmServerKey == null || fcmServerKey.isEmpty()) {
            logger.error("❌ FCM server key not configured");
            return false;
        }

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                // Build FCM payload
                Map<String, Object> notification = new HashMap<>();
                notification.put("title", pushDto.getTitle());
                notification.put("body", pushDto.getMessage());
                notification.put("icon", "default");
                notification.put("sound", "default");

                Map<String, Object> payload = new HashMap<>();
                payload.put("to", pushDto.getRecipient());
                payload.put("notification", notification);
                payload.put("priority", "high");

                // Add custom data if provided
                if (pushDto.getDataPayload() != null && !pushDto.getDataPayload().isEmpty()) {
                    try {
                        Map<String, Object> customData = objectMapper.readValue(
                                pushDto.getDataPayload(), Map.class);
                        payload.put("data", customData);
                    } catch (Exception e) {
                        logger.warn("⚠️ Invalid JSON in data payload, sending as string");
                        Map<String, String> data = new HashMap<>();
                        data.put("custom_data", pushDto.getDataPayload());
                        payload.put("data", data);
                    }
                }

                // Set headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "key=" + fcmServerKey);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                // Send request
                ResponseEntity<Map> response = restTemplate.exchange(
                        fcmUrl, HttpMethod.POST, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("✅ FCM push sent successfully (attempt: {})", attempt);
                    return true;
                } else {
                    logger.warn("⚠️ FCM push failed with status: {} (attempt: {}/{})",
                            response.getStatusCode(), attempt, maxRetryAttempts);
                }

            } catch (Exception e) {
                logger.warn("⚠️ FCM push attempt {}/{} failed: {}",
                        attempt, maxRetryAttempts, e.getMessage());

                if (attempt == maxRetryAttempts) {
                    logger.error("💥 All FCM push attempts failed");
                    return false;
                }

                // Wait before retry
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Send push notification via OneSignal
     */
    private boolean sendOneSignalPush(PushNotificationDto pushDto) {
        if (oneSignalAppId == null || oneSignalAppId.isEmpty() ||
                oneSignalApiKey == null || oneSignalApiKey.isEmpty()) {
            logger.error("❌ OneSignal configuration not complete");
            return false;
        }

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                // Build OneSignal payload
                Map<String, Object> payload = new HashMap<>();
                payload.put("app_id", oneSignalAppId);
                payload.put("include_player_ids", Arrays.asList(pushDto.getRecipient()));

                Map<String, String> contents = new HashMap<>();
                contents.put("en", pushDto.getMessage());
                payload.put("contents", contents);

                Map<String, String> headings = new HashMap<>();
                headings.put("en", pushDto.getTitle());
                payload.put("headings", headings);

                // Add custom data if provided
                if (pushDto.getDataPayload() != null && !pushDto.getDataPayload().isEmpty()) {
                    try {
                        Map<String, Object> customData = objectMapper.readValue(
                                pushDto.getDataPayload(), Map.class);
                        payload.put("data", customData);
                    } catch (Exception e) {
                        logger.warn("⚠️ Invalid JSON in data payload for OneSignal");
                    }
                }

                // Set headers
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Basic " + oneSignalApiKey);

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                // Send request
                ResponseEntity<Map> response = restTemplate.exchange(
                        oneSignalUrl, HttpMethod.POST, request, Map.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("✅ OneSignal push sent successfully (attempt: {})", attempt);
                    return true;
                } else {
                    logger.warn("⚠️ OneSignal push failed with status: {} (attempt: {}/{})",
                            response.getStatusCode(), attempt, maxRetryAttempts);
                }

            } catch (Exception e) {
                logger.warn("⚠️ OneSignal push attempt {}/{} failed: {}",
                        attempt, maxRetryAttempts, e.getMessage());

                if (attempt == maxRetryAttempts) {
                    logger.error("💥 All OneSignal push attempts failed");
                    return false;
                }

                // Wait before retry
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Send batch push notifications (for multiple devices)
     */
    public Map<String, Boolean> sendBatchPush(List<PushNotificationDto> pushNotifications) {
        logger.info("📦 Sending batch push notifications: {} devices", pushNotifications.size());

        Map<String, Boolean> results = new HashMap<>();

        // Process in batches to avoid overwhelming the service
        for (int i = 0; i < pushNotifications.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, pushNotifications.size());
            List<PushNotificationDto> batch = pushNotifications.subList(i, endIndex);

            logger.info("📲 Processing batch {}-{} of {}", i + 1, endIndex, pushNotifications.size());

            for (PushNotificationDto pushDto : batch) {
                boolean success = sendPush(pushDto);
                results.put(pushDto.getRecipient(), success);

                // Small delay between requests to avoid rate limiting
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        long successCount = results.values().stream().mapToLong(success -> success ? 1 : 0).sum();
        logger.info("📊 Batch push complete: {}/{} successful", successCount, results.size());

        return results;
    }

    /**
     * Send topic-based push notification (to all subscribers of a topic)
     */
    public boolean sendTopicPush(String topic, String title, String message, Map<String, Object> data) {
        if (!pushProvider.equals("FCM")) {
            logger.warn("⚠️ Topic push only supported with FCM provider");
            return false;
        }

        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", message);

            Map<String, Object> payload = new HashMap<>();
            payload.put("to", "/topics/" + topic);
            payload.put("notification", notification);
            payload.put("priority", "high");

            if (data != null && !data.isEmpty()) {
                payload.put("data", data);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "key=" + fcmServerKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    fcmUrl, HttpMethod.POST, request, Map.class);

            boolean success = response.getStatusCode().is2xxSuccessful();

            if (success) {
                logger.info("✅ Topic push sent successfully to: {}", topic);
            } else {
                logger.error("❌ Topic push failed for: {}", topic);
            }

            return success;

        } catch (Exception e) {
            logger.error("💥 Failed to send topic push to {}: {}", topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Simulate push delivery for testing
     */
    private boolean simulatePushDelivery(PushNotificationDto pushDto) {
        logger.info("🧪 TEST MODE: Simulating push to {} with title: '{}' and message: '{}'",
                maskDeviceToken(pushDto.getRecipient()), pushDto.getTitle(), pushDto.getMessage());

        // Simulate processing time
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate random failures for testing (5% failure rate)
        boolean success = Math.random() > 0.05;

        if (success) {
            logger.info("✅ TEST MODE: Push delivery simulated successfully");
        } else {
            logger.warn("❌ TEST MODE: Push delivery simulation failed");
        }

        return success;
    }

    /**
     * Validate device token format
     */
    public boolean isValidDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            return false;
        }

        // Basic validation - tokens are usually long alphanumeric strings
        return deviceToken.length() >= 50 && deviceToken.matches("^[a-zA-Z0-9_:.-]+$");
    }

    /**
     * Mask device token for logging (security)
     */
    private String maskDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.length() < 10) {
            return "***";
        }
        return deviceToken.substring(0, 6) + "***" + deviceToken.substring(deviceToken.length() - 4);
    }

    /**
     * Get push service status
     */
    public boolean isServiceAvailable() {
        return switch (pushProvider.toUpperCase()) {
            case "FCM" -> fcmServerKey != null && !fcmServerKey.isEmpty();
            case "ONESIGNAL" -> oneSignalAppId != null && !oneSignalAppId.isEmpty() &&
                    oneSignalApiKey != null && !oneSignalApiKey.isEmpty();
            default -> true; // Simulation mode is always available
        };
    }

    /**
     * Get detailed service status
     */
    public String getServiceStatus() {
        if (testMode) {
            return "Push Service: Test Mode Active";
        }

        return switch (pushProvider.toUpperCase()) {
            case "FCM" -> "Push Service: Active (Firebase Cloud Messaging)";
            case "ONESIGNAL" -> "Push Service: Active (OneSignal)";
            default -> "Push Service: Simulation Mode";
        };
    }

    /**
     * Get service configuration summary
     */
    public Map<String, Object> getServiceInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("provider", pushProvider);
        info.put("testMode", testMode);
        info.put("available", isServiceAvailable());
        info.put("maxRetryAttempts", maxRetryAttempts);
        info.put("batchSize", batchSize);
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PushService that = (PushService) o;
        return Objects.equals(pushProvider, that.pushProvider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pushProvider);
    }

    @Override
    public String toString() {
        return "PushService{" +
                "provider='" + pushProvider + '\'' +
                ", testMode=" + testMode +
                ", maxRetryAttempts=" + maxRetryAttempts +
                ", batchSize=" + batchSize +
                ", available=" + isServiceAvailable() +
                '}';
    }
}
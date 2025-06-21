package com.cabinet360.core.service;

import com.cabinet360.core.config.TwilioConfig;
import com.cabinet360.core.dto.SmsNotificationDto;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.exception.AuthenticationException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Objects;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    private final TwilioConfig twilioConfig;

    @Value("${sms.retry.attempts:3}")
    private int maxRetryAttempts;

    @Value("${sms.rate.limit:false}")
    private boolean rateLimitEnabled;

    @Value("${sms.test.mode:false}")
    private boolean testMode;

    public SmsService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @PostConstruct
    public void initializeTwilio() {
        try {
            if (twilioConfig.isEnabled() && !testMode) {
                Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
                logger.info("✅ Twilio SMS service initialized successfully");
            } else {
                logger.info("📱 SMS service running in test mode or disabled");
            }
        } catch (Exception e) {
            logger.error("💥 Failed to initialize Twilio: {}", e.getMessage(), e);
        }
    }

    /**
     * Send SMS notification - Main integration point with NotificationService
     */
    public boolean sendSms(SmsNotificationDto smsDto) {
        try {
            logger.info("📱 Sending SMS to: {}", smsDto.getRecipient());

            if (testMode) {
                return simulateSmsDelivery(smsDto);
            }

            if (!twilioConfig.isEnabled()) {
                logger.warn("⚠️ SMS service is disabled");
                return false;
            }

            return sendSmsWithRetry(smsDto.getRecipient(), smsDto.getMessage());

        } catch (Exception e) {
            logger.error("💥 Failed to send SMS to {}: {}", smsDto.getRecipient(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send appointment reminder SMS
     */
    public boolean sendAppointmentReminder(String phoneNumber, String patientName,
                                           String doctorName, String appointmentDate,
                                           String appointmentTime) {
        String message = String.format(
                "Bonjour %s, rappel de votre RDV avec Dr. %s le %s à %s. Cabinet360",
                patientName, doctorName, appointmentDate, appointmentTime
        );

        return sendSmsWithRetry(phoneNumber, message);
    }

    /**
     * Send appointment confirmation SMS
     */
    public boolean sendAppointmentConfirmation(String phoneNumber, String patientName,
                                               String doctorName, String appointmentDate,
                                               String appointmentTime) {
        String message = String.format(
                "RDV confirmé! Bonjour %s, votre RDV avec Dr. %s est fixé le %s à %s. Cabinet360",
                patientName, doctorName, appointmentDate, appointmentTime
        );

        return sendSmsWithRetry(phoneNumber, message);
    }

    /**
     * Send appointment cancellation SMS
     */
    public boolean sendAppointmentCancellation(String phoneNumber, String patientName,
                                               String doctorName, String appointmentDate,
                                               String reason) {
        String message = String.format(
                "RDV annulé. Bonjour %s, votre RDV avec Dr. %s du %s a été annulé. Raison: %s. Cabinet360",
                patientName, doctorName, appointmentDate, reason
        );

        return sendSmsWithRetry(phoneNumber, message);
    }

    /**
     * Send verification code SMS
     */
    public boolean sendVerificationCode(String phoneNumber, String code, int expiryMinutes) {
        String message = String.format(
                "Votre code de vérification Cabinet360: %s. Code valide %d minutes. Ne le partagez pas.",
                code, expiryMinutes
        );

        return sendSmsWithRetry(phoneNumber, message);
    }

    /**
     * Send password reset code SMS
     */
    public boolean sendPasswordResetCode(String phoneNumber, String resetCode) {
        String message = String.format(
                "Code de réinitialisation Cabinet360: %s. Valide 10 minutes. Ne le partagez pas.",
                resetCode
        );

        return sendSmsWithRetry(phoneNumber, message);
    }

    /**
     * Core SMS sending with retry logic
     */
    private boolean sendSmsWithRetry(String phoneNumber, String messageContent) {
        if (!isValidPhoneNumber(phoneNumber)) {
            logger.error("❌ Invalid phone number format: {}", phoneNumber);
            return false;
        }

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                Message message = Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber(twilioConfig.getPhoneNumber()),
                        messageContent
                ).create();

                logger.info("✅ SMS sent successfully to {} (SID: {}, attempt: {})",
                        phoneNumber, message.getSid(), attempt);
                return true;

            } catch (AuthenticationException e) {
                logger.error("🔐 Twilio authentication failed: {}", e.getMessage());
                return false; // Don't retry auth failures

            } catch (ApiException e) {
                logger.warn("⚠️ SMS sending failed (attempt {}/{}): {} - {}",
                        attempt, maxRetryAttempts, e.getCode(), e.getMessage());

                if (attempt == maxRetryAttempts) {
                    logger.error("💥 All SMS sending attempts failed for: {}", phoneNumber);
                    return false;
                }

                // Wait before retry with exponential backoff
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }

            } catch (Exception e) {
                logger.error("💥 Unexpected error sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
                return false;
            }
        }

        return false;
    }

    /**
     * Simulate SMS delivery for testing
     */
    private boolean simulateSmsDelivery(SmsNotificationDto smsDto) {
        logger.info("🧪 TEST MODE: Simulating SMS to {} with message: '{}'",
                smsDto.getRecipient(), smsDto.getMessage());

        // Simulate random failures for testing (10% failure rate)
        boolean success = Math.random() > 0.1;

        if (success) {
            logger.info("✅ TEST MODE: SMS delivery simulated successfully");
        } else {
            logger.warn("❌ TEST MODE: SMS delivery simulation failed");
        }

        return success;
    }

    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        // Remove spaces and special characters for validation
        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Check for international format (+country code) or local format
        return cleaned.matches("^\\+?[1-9]\\d{7,14}$");
    }

    /**
     * Format phone number to international format
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        String cleaned = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Add + if missing and number doesn't start with country code
        if (!cleaned.startsWith("+") && cleaned.length() >= 10) {
            // Assume Moroccan number if no country code
            if (cleaned.startsWith("0")) {
                cleaned = "+212" + cleaned.substring(1);
            } else if (!cleaned.startsWith("212")) {
                cleaned = "+212" + cleaned;
            } else {
                cleaned = "+" + cleaned;
            }
        }

        return cleaned;
    }

    /**
     * Get SMS service status
     */
    public boolean isServiceAvailable() {
        return twilioConfig.isEnabled() && twilioConfig.getAccountSid() != null;
    }

    /**
     * Get remaining SMS quota (if available from Twilio)
     */
    public String getServiceStatus() {
        if (testMode) {
            return "SMS Service: Test Mode Active";
        } else if (twilioConfig.isEnabled()) {
            return "SMS Service: Active (Twilio)";
        } else {
            return "SMS Service: Disabled";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmsService that = (SmsService) o;
        return Objects.equals(twilioConfig.getAccountSid(), that.twilioConfig.getAccountSid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(twilioConfig.getAccountSid());
    }

    @Override
    public String toString() {
        return "SmsService{" +
                "enabled=" + twilioConfig.isEnabled() +
                ", testMode=" + testMode +
                ", maxRetryAttempts=" + maxRetryAttempts +
                ", rateLimitEnabled=" + rateLimitEnabled +
                ", fromNumber='" + (twilioConfig.getPhoneNumber() != null ? "***" + twilioConfig.getPhoneNumber().substring(twilioConfig.getPhoneNumber().length()-4) : "null") + '\'' +
                '}';
    }
}
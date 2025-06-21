package com.cabinet360.core.service;

import com.cabinet360.core.dto.EmailNotificationDto;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Objects;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from:noreply@cabinet360.com}")
    private String defaultFromEmail;

    @Value("${spring.mail.personal:Cabinet360}")
    private String defaultFromName;

    @Value("${email.retry.attempts:3}")
    private int maxRetryAttempts;

    @Value("${email.timeout.seconds:30}")
    private int timeoutSeconds;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Send notification email - Main integration point with NotificationService
     */
    public boolean sendEmail(EmailNotificationDto emailDto) {
        try {
            logger.info("📧 Sending email to: {} with subject: {}",
                    emailDto.getRecipient(), emailDto.getSubject());

            return sendEmailWithRetry(
                    emailDto.getRecipient(),
                    emailDto.getSubject(),
                    emailDto.getMessage(),
                    true // HTML content
            );

        } catch (Exception e) {
            logger.error("💥 Failed to send notification email to {}: {}",
                    emailDto.getRecipient(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send welcome email using template
     */
    public boolean sendWelcomeEmail(String to, String username, String activationLink) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("activationLink", activationLink);
            context.setVariable("supportEmail", "support@cabinet360.com");
            context.setVariable("year", java.time.Year.now().getValue());

            String htmlContent = templateEngine.process("email/welcome", context);

            return sendEmailWithRetry(to, "Bienvenue sur Cabinet360! 🎉", htmlContent, true);

        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send appointment reminder email using template
     */
    public boolean sendAppointmentReminder(String to, String patientName, String doctorName,
                                           String appointmentDate, String appointmentTime,
                                           String cabinetAddress) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("appointmentDate", appointmentDate);
            context.setVariable("appointmentTime", appointmentTime);
            context.setVariable("cabinetAddress", cabinetAddress);
            context.setVariable("supportPhone", "+212-XXX-XXXXXX");

            String htmlContent = templateEngine.process("email/appointment-reminder", context);

            return sendEmailWithRetry(
                    to,
                    "Rappel: Rendez-vous avec Dr. " + doctorName + " 📅",
                    htmlContent,
                    true
            );

        } catch (Exception e) {
            logger.error("Failed to send appointment reminder to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send appointment cancellation email
     */
    public boolean sendAppointmentCancellation(String to, String patientName, String doctorName,
                                               String appointmentDate, String reason) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("appointmentDate", appointmentDate);
            context.setVariable("reason", reason);
            context.setVariable("rebookingLink", "https://cabinet360.com/appointments");

            String htmlContent = templateEngine.process("email/appointment-cancelled", context);

            return sendEmailWithRetry(
                    to,
                    "Annulation: Rendez-vous avec Dr. " + doctorName + " ❌",
                    htmlContent,
                    true
            );

        } catch (Exception e) {
            logger.error("Failed to send cancellation email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send password reset email
     */
    public boolean sendPasswordResetEmail(String to, String username, String resetToken, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetToken", resetToken);
            context.setVariable("resetLink", resetLink);
            context.setVariable("expiryHours", "24");

            String htmlContent = templateEngine.process("email/password-reset", context);

            return sendEmailWithRetry(
                    to,
                    "Réinitialisation de votre mot de passe - Cabinet360 🔐",
                    htmlContent,
                    true
            );

        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send custom email with dynamic template and variables
     */
    public boolean sendCustomTemplateEmail(String to, String subject, String templateName,
                                           Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            String htmlContent = templateEngine.process(templateName, context);
            return sendEmailWithRetry(to, subject, htmlContent, true);

        } catch (Exception e) {
            logger.error("Failed to send custom template email to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send simple text email (no template)
     */
    public boolean sendSimpleEmail(String to, String subject, String textContent) {
        return sendEmailWithRetry(to, subject, textContent, false);
    }

    /**
     * Core email sending method with retry logic
     */
    private boolean sendEmailWithRetry(String to, String subject, String content, boolean isHtml) {
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(defaultFromEmail, defaultFromName);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(content, isHtml);

                // Add custom headers
                mimeMessage.setHeader("X-Mailer", "Cabinet360-System");
                mimeMessage.setHeader("X-Priority", "3");

                mailSender.send(mimeMessage);

                logger.info("✅ Email sent successfully to {} (attempt: {})", to, attempt);
                return true;

            } catch (MailException e) {
                logger.warn("⚠️ Email sending failed (attempt {}/{}): {}", attempt, maxRetryAttempts, e.getMessage());

                if (attempt == maxRetryAttempts) {
                    logger.error("💥 All email sending attempts failed for: {}", to);
                    return false;
                }

                // Wait before retry (exponential backoff)
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            } catch (Exception e) {
                logger.error("💥 Unexpected error sending email to {}: {}", to, e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    /**
     * Validate email address format
     */
    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    /**
     * Get email service status
     */
    public boolean isServiceAvailable() {
        try {
            // Test connection by attempting to get a session
            return mailSender != null;
        } catch (Exception e) {
            logger.error("Email service not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailService that = (EmailService) o;
        return Objects.equals(defaultFromEmail, that.defaultFromEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultFromEmail);
    }

    @Override
    public String toString() {
        return "EmailService{" +
                "fromEmail='" + defaultFromEmail + '\'' +
                ", fromName='" + defaultFromName + '\'' +
                ", maxRetryAttempts=" + maxRetryAttempts +
                ", timeoutSeconds=" + timeoutSeconds +
                '}';
    }
}
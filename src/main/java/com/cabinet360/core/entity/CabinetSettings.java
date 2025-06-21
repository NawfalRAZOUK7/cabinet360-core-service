package com.cabinet360.core.entity;

import com.cabinet360.core.dto.CabinetSettingsDto;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CabinetSettings {

    /**
     * Default appointment duration in minutes
     */
    @Column(name = "default_appointment_duration")
    private Integer defaultAppointmentDuration = 30;

    /**
     * Working days (e.g., "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY")
     */
    @Column(name = "working_days", length = 200)
    private String workingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";

    /**
     * Working hours start (e.g., "08:00")
     */
    @Column(name = "working_hours_start", length = 10)
    private String workingHoursStart = "08:00";

    /**
     * Working hours end (e.g., "18:00")
     */
    @Column(name = "working_hours_end", length = 10)
    private String workingHoursEnd = "18:00";

    /**
     * Allow online booking by patients
     */
    @Column(name = "allow_online_booking")
    private Boolean allowOnlineBooking = true;

    /**
     * Maximum patients per day
     */
    @Column(name = "max_patients_per_day")
    private Integer maxPatientsPerDay = 50;

    /**
     * Advance booking days (how many days in advance patients can book)
     */
    @Column(name = "advance_booking_days")
    private Integer advanceBookingDays = 30;

    /**
     * Require patient confirmation for appointments
     */
    @Column(name = "require_patient_confirmation")
    private Boolean requirePatientConfirmation = true;

    /**
     * Send appointment reminders
     */
    @Column(name = "send_reminders")
    private Boolean sendReminders = true;

    /**
     * Reminder hours before appointment
     */
    @Column(name = "reminder_hours_before")
    private Integer reminderHoursBefore = 24;

    /**
     * Emergency contact number
     */
    @Column(name = "emergency_contact", length = 50)
    private String emergencyContact;

    // === Constructors ===

    public CabinetSettings() {}

    // === Builder Pattern ===
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer defaultAppointmentDuration = 30;
        private String workingDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY";
        private String workingHoursStart = "08:00";
        private String workingHoursEnd = "18:00";
        private Boolean allowOnlineBooking = true;
        private Integer maxPatientsPerDay = 50;
        private Integer advanceBookingDays = 30;
        private Boolean requirePatientConfirmation = true;
        private Boolean sendReminders = true;
        private Integer reminderHoursBefore = 24;
        private String emergencyContact;

        public Builder defaultAppointmentDuration(Integer duration) {
            this.defaultAppointmentDuration = duration;
            return this;
        }

        public Builder workingDays(String workingDays) {
            this.workingDays = workingDays;
            return this;
        }

        public Builder workingHours(String start, String end) {
            this.workingHoursStart = start;
            this.workingHoursEnd = end;
            return this;
        }

        public Builder allowOnlineBooking(Boolean allow) {
            this.allowOnlineBooking = allow;
            return this;
        }

        public Builder maxPatientsPerDay(Integer max) {
            this.maxPatientsPerDay = max;
            return this;
        }

        public Builder advanceBookingDays(Integer days) {
            this.advanceBookingDays = days;
            return this;
        }

        public Builder requirePatientConfirmation(Boolean require) {
            this.requirePatientConfirmation = require;
            return this;
        }

        public Builder reminders(Boolean send, Integer hoursBefore) {
            this.sendReminders = send;
            this.reminderHoursBefore = hoursBefore;
            return this;
        }

        public Builder emergencyContact(String contact) {
            this.emergencyContact = contact;
            return this;
        }

        public CabinetSettings build() {
            CabinetSettings settings = new CabinetSettings();
            settings.defaultAppointmentDuration = this.defaultAppointmentDuration;
            settings.workingDays = this.workingDays;
            settings.workingHoursStart = this.workingHoursStart;
            settings.workingHoursEnd = this.workingHoursEnd;
            settings.allowOnlineBooking = this.allowOnlineBooking;
            settings.maxPatientsPerDay = this.maxPatientsPerDay;
            settings.advanceBookingDays = this.advanceBookingDays;
            settings.requirePatientConfirmation = this.requirePatientConfirmation;
            settings.sendReminders = this.sendReminders;
            settings.reminderHoursBefore = this.reminderHoursBefore;
            settings.emergencyContact = this.emergencyContact;
            return settings;
        }
    }

    // === Getters and Setters ===

    public Integer getDefaultAppointmentDuration() { return defaultAppointmentDuration; }
    public void setDefaultAppointmentDuration(Integer defaultAppointmentDuration) { 
        this.defaultAppointmentDuration = defaultAppointmentDuration; 
    }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }

    public String getWorkingHoursStart() { return workingHoursStart; }
    public void setWorkingHoursStart(String workingHoursStart) { this.workingHoursStart = workingHoursStart; }

    public String getWorkingHoursEnd() { return workingHoursEnd; }
    public void setWorkingHoursEnd(String workingHoursEnd) { this.workingHoursEnd = workingHoursEnd; }

    public Boolean getAllowOnlineBooking() { return allowOnlineBooking; }
    public void setAllowOnlineBooking(Boolean allowOnlineBooking) { this.allowOnlineBooking = allowOnlineBooking; }

    public Integer getMaxPatientsPerDay() { return maxPatientsPerDay; }
    public void setMaxPatientsPerDay(Integer maxPatientsPerDay) { this.maxPatientsPerDay = maxPatientsPerDay; }

    public Integer getAdvanceBookingDays() { return advanceBookingDays; }
    public void setAdvanceBookingDays(Integer advanceBookingDays) { this.advanceBookingDays = advanceBookingDays; }

    public Boolean getRequirePatientConfirmation() { return requirePatientConfirmation; }
    public void setRequirePatientConfirmation(Boolean requirePatientConfirmation) { 
        this.requirePatientConfirmation = requirePatientConfirmation; 
    }

    public Boolean getSendReminders() { return sendReminders; }
    public void setSendReminders(Boolean sendReminders) { this.sendReminders = sendReminders; }

    public Integer getReminderHoursBefore() { return reminderHoursBefore; }
    public void setReminderHoursBefore(Integer reminderHoursBefore) { 
        this.reminderHoursBefore = reminderHoursBefore; 
    }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    // === equals, hashCode, toString ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CabinetSettings that = (CabinetSettings) o; // ✅ Cast to same type
        return Objects.equals(defaultAppointmentDuration, that.getDefaultAppointmentDuration()) &&
                Objects.equals(workingDays, that.getWorkingDays()) &&
                Objects.equals(workingHoursStart, that.getWorkingHoursStart()) &&
                Objects.equals(workingHoursEnd, that.getWorkingHoursEnd()) &&
                Objects.equals(allowOnlineBooking, that.getAllowOnlineBooking()) &&
                Objects.equals(maxPatientsPerDay, that.getMaxPatientsPerDay()) &&
                Objects.equals(advanceBookingDays, that.getAdvanceBookingDays()) &&
                Objects.equals(requirePatientConfirmation, that.getRequirePatientConfirmation()) &&
                Objects.equals(sendReminders, that.getSendReminders()) &&
                Objects.equals(reminderHoursBefore, that.getReminderHoursBefore()) &&
                Objects.equals(emergencyContact, that.getEmergencyContact());
    }

    @Override
    public int hashCode() {
        return Objects.hash(defaultAppointmentDuration, workingDays, workingHoursStart,
                workingHoursEnd, allowOnlineBooking, maxPatientsPerDay,
                advanceBookingDays, requirePatientConfirmation, sendReminders,
                reminderHoursBefore, emergencyContact);
    }

    @Override
    public String toString() {
        return "CabinetSettingsDto{" +
                "defaultAppointmentDuration=" + defaultAppointmentDuration +
                ", workingDays='" + workingDays + '\'' +
                ", workingHours='" + workingHoursStart + "-" + workingHoursEnd + '\'' +
                ", allowOnlineBooking=" + allowOnlineBooking +
                ", maxPatientsPerDay=" + maxPatientsPerDay +
                '}';
    }
}
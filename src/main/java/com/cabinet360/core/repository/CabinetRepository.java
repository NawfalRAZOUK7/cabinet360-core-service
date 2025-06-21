package com.cabinet360.core.repository;

import com.cabinet360.core.entity.Cabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CabinetRepository extends JpaRepository<Cabinet, Long> {

    /**
     * Find all cabinets owned by a specific doctor
     */
    List<Cabinet> findByOwnerDoctorId(Long ownerDoctorId);

    /**
     * Find all active cabinets owned by a specific doctor
     */
    List<Cabinet> findByOwnerDoctorIdAndStatusAndDeletedAtIsNull(Long ownerDoctorId, String status);

    /**
     * Find cabinet by name and owner (for uniqueness validation)
     */
    Optional<Cabinet> findByOwnerDoctorIdAndCabinetName(Long ownerDoctorId, String cabinetName);

    /**
     * Check if cabinet name exists for an owner
     */
    boolean existsByOwnerDoctorIdAndCabinetName(Long ownerDoctorId, String cabinetName);

    /**
     * Find all active cabinets (not soft deleted)
     */
    List<Cabinet> findByStatusAndDeletedAtIsNull(String status);

    /**
     * Find all cabinets by status
     */
    List<Cabinet> findByStatus(String status);

    /**
     * Find cabinets created after a specific date
     */
    List<Cabinet> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find cabinets that allow online booking
     */
    @Query("SELECT c FROM Cabinet c WHERE c.settings.allowOnlineBooking = true AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    List<Cabinet> findCabinetsWithOnlineBooking();

    /**
     * Find cabinets by working days pattern
     */
    @Query("SELECT c FROM Cabinet c WHERE c.settings.workingDays LIKE %:day% AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    List<Cabinet> findCabinetsByWorkingDay(@Param("day") String day);

    /**
     * Find cabinets with specific appointment duration
     */
    @Query("SELECT c FROM Cabinet c WHERE c.settings.defaultAppointmentDuration = :duration AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    List<Cabinet> findCabinetsByAppointmentDuration(@Param("duration") Integer duration);

    /**
     * Count active cabinets for a doctor
     */
    @Query("SELECT COUNT(c) FROM Cabinet c WHERE c.ownerDoctorId = :doctorId AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    long countActiveCabinetsByDoctor(@Param("doctorId") Long doctorId);

    /**
     * Find cabinets updated since a specific date
     */
    List<Cabinet> findByUpdatedAtAfter(LocalDateTime date);

    /**
     * Find soft-deleted cabinets (for potential recovery)
     */
    List<Cabinet> findByDeletedAtIsNotNull();

    /**
     * Search cabinets by name pattern
     */
    @Query("SELECT c FROM Cabinet c WHERE LOWER(c.cabinetName) LIKE LOWER(CONCAT('%', :name, '%')) AND c.deletedAt IS NULL")
    List<Cabinet> searchByName(@Param("name") String name);

    /**
     * Find cabinets requiring patient confirmation
     */
    @Query("SELECT c FROM Cabinet c WHERE c.settings.requirePatientConfirmation = true AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    List<Cabinet> findCabinetsRequiringConfirmation();
}
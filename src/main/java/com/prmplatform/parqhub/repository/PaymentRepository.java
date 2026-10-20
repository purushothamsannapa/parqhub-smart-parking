package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by booking ID
    Optional<Payment> findByBookingId(Long bookingId);

    // Find payments by user ID (through booking relationship)
    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId")
    List<Payment> findByUserId(Long userId);

    // Sum amount for completed payments for a user after a timestamp
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'Completed' AND p.booking.user.id = :userId AND p.timestamp >= :timestamp")
    Double sumAmountByUserIdAndCompletedAndTimestampAfter(Long userId, LocalDateTime timestamp);

    // Original method for total spent today (all users)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'Completed' AND p.timestamp >= :startOfDay")
    Double sumAmountByCompletedAndTimestampAfter(LocalDateTime startOfDay);

    @Query("SELECT p FROM Payment p WHERE LOWER(p.method) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.status) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Payment> findByMethodContainingIgnoreCaseOrStatusContainingIgnoreCase(@Param("search") String search, @Param("search") String search2, Pageable pageable);

    // Find payments within date range
    @Query("SELECT p FROM Payment p WHERE p.timestamp >= :startDate AND p.timestamp <= :endDate ORDER BY p.timestamp DESC")
    List<Payment> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find payments within date range with pagination
    @Query("SELECT p FROM Payment p WHERE p.timestamp >= :startDate AND p.timestamp <= :endDate ORDER BY p.timestamp DESC")
    Page<Payment> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // Sum total amount within date range for completed payments
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'Completed' AND p.timestamp >= :startDate AND p.timestamp <= :endDate")
    Double sumAmountByCompletedAndTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count payments by status within date range
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.timestamp >= :startDate AND p.timestamp <= :endDate")
    Long countByStatusAndTimestampBetween(@Param("status") Payment.PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get payment method distribution within date range
    @Query("SELECT p.method, COUNT(p) FROM Payment p WHERE p.timestamp >= :startDate AND p.timestamp <= :endDate GROUP BY p.method")
    List<Object[]> getPaymentMethodDistribution(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get revenue trend by day within date range
    @Query("SELECT DATE(p.timestamp), SUM(p.amount) FROM Payment p WHERE p.status = 'Completed' AND p.timestamp >= :startDate AND p.timestamp <= :endDate GROUP BY DATE(p.timestamp) ORDER BY DATE(p.timestamp)")
    List<Object[]> getRevenueTrendByDay(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get all payments summary (for initial report)
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'Completed'")
    Double getTotalCompletedRevenue();

    // Get payments filtered by city
    @Query("SELECT p FROM Payment p JOIN p.booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE pl.city = :city AND p.timestamp >= :startDate AND p.timestamp <= :endDate ORDER BY p.timestamp DESC")
    List<Payment> findByCityAndTimestampBetween(@Param("city") String city, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Get payments filtered by city and location
    @Query("SELECT p FROM Payment p JOIN p.booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE pl.city = :city AND pl.location = :location AND p.timestamp >= :startDate AND p.timestamp <= :endDate ORDER BY p.timestamp DESC")
    List<Payment> findByCityLocationAndTimestampBetween(@Param("city") String city, @Param("location") String location, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Sum revenue by city within date range
    @Query("SELECT pl.city, SUM(p.amount) FROM Payment p JOIN p.booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE p.status = 'Completed' AND p.timestamp >= :startDate AND p.timestamp <= :endDate GROUP BY pl.city")
    List<Object[]> getRevenueByCity(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Sum revenue by city and location within date range
    @Query("SELECT pl.city, pl.location, SUM(p.amount) FROM Payment p JOIN p.booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE p.status = 'Completed' AND p.timestamp >= :startDate AND p.timestamp <= :endDate GROUP BY pl.city, pl.location")
    List<Object[]> getRevenueByCityAndLocation(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count payments by status for city within date range
    @Query("SELECT COUNT(p) FROM Payment p JOIN p.booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE p.status = :status AND pl.city = :city AND p.timestamp >= :startDate AND p.timestamp <= :endDate")
    Long countByStatusCityAndTimestampBetween(@Param("status") Payment.PaymentStatus status, @Param("city") String city, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count payments by status for city and location within date range
    @Query("SELECT COUNT(p) FROM Payment p JOIN p.booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE p.status = :status AND pl.city = :city AND pl.location = :location AND p.timestamp >= :startDate AND p.timestamp <= :endDate")
    Long countByStatusCityLocationAndTimestampBetween(@Param("status") Payment.PaymentStatus status, @Param("city") String city, @Param("location") String location, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
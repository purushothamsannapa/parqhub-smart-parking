package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    List<Booking> findByUserIdOrderByStartTimeDesc(Long userId);
    List<Booking> findByVehicleId(Long vehicleId);
    long countByPaymentStatus(Booking.PaymentStatus paymentStatus);
    
    // Add this method to find bookings by parking slot ID
    List<Booking> findByParkingSlotId(Long parkingSlotId);
    
    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.parkingSlot ps " +
           "LEFT JOIN FETCH ps.parkingLot pl " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.startTime DESC")
    List<Booking> findByUserIdWithParkingDetailsOrderByStartTimeDesc(@Param("userId") Long userId);

    Page<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus, Pageable pageable);

    // Find bookings within date range
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :startDate AND b.startTime <= :endDate ORDER BY b.startTime DESC")
    List<Booking> findByStartTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find bookings within date range with pagination
    @Query("SELECT b FROM Booking b WHERE b.startTime >= :startDate AND b.startTime <= :endDate ORDER BY b.startTime DESC")
    Page<Booking> findByStartTimeBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // Count bookings by payment status within date range
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.paymentStatus = :status AND b.startTime >= :startDate AND b.startTime <= :endDate")
    Long countByPaymentStatusAndStartTimeBetween(@Param("status") Booking.PaymentStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find bookings by city within date range
    @Query("SELECT b FROM Booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE pl.city = :city AND b.startTime >= :startDate AND b.startTime <= :endDate ORDER BY b.startTime DESC")
    List<Booking> findByCityAndStartTimeBetween(@Param("city") String city, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find bookings by city and location within date range
    @Query("SELECT b FROM Booking b JOIN b.parkingSlot ps JOIN ps.parkingLot pl WHERE pl.city = :city AND pl.location = :location AND b.startTime >= :startDate AND b.startTime <= :endDate ORDER BY b.startTime DESC")
    List<Booking> findByCityLocationAndStartTimeBetween(@Param("city") String city, @Param("location") String location, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
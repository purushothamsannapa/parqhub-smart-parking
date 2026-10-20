package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.ParkingSlot;
import com.prmplatform.parqhub.model.ParkingSlot.SlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    List<ParkingSlot> findByParkingLotId(Long lotId);
    List<ParkingSlot> findByParkingLotIdAndStatus(Long lotId, SlotStatus status);
    long countByParkingLotIdAndStatus(Long lotId, SlotStatus status);
    long countByStatus(SlotStatus status);
    
    // Add this method to find parking slots with their associated parking lot
    List<ParkingSlot> findByParkingLotIdOrderByStatusAsc(Long parkingLotId);

    @Query("SELECT p.parkingLot.city, COUNT(p) FROM ParkingSlot p WHERE p.status = :status GROUP BY p.parkingLot.city")
    List<Object[]> countByCityAndStatus(@org.springframework.data.repository.query.Param("status") SlotStatus status);

    Page<ParkingSlot> findByStatus(SlotStatus status, Pageable pageable);

    // Enhanced queries for occupancy reporting
    @Query("SELECT ps FROM ParkingSlot ps JOIN FETCH ps.parkingLot")
    List<ParkingSlot> findAllWithParkingLot();

    @Query("SELECT ps.parkingLot.city, ps.status, COUNT(ps) FROM ParkingSlot ps GROUP BY ps.parkingLot.city, ps.status")
    List<Object[]> getSlotStatusDistributionByCity();

    @Query("SELECT ps.parkingLot.city, ps.parkingLot.location, COUNT(ps), " +
           "SUM(CASE WHEN ps.status = 'AVAILABLE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN ps.status = 'BOOKED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN ps.status = 'OCCUPIED' THEN 1 ELSE 0 END) " +
           "FROM ParkingSlot ps GROUP BY ps.parkingLot.city, ps.parkingLot.location")
    List<Object[]> getDetailedSlotStatusByLocation();

    @Query("SELECT COUNT(ps) FROM ParkingSlot ps WHERE ps.parkingLot.city = :city AND ps.status = :status")
    long countByCityAndStatusParam(@org.springframework.data.repository.query.Param("city") String city, @org.springframework.data.repository.query.Param("status") SlotStatus status);
}
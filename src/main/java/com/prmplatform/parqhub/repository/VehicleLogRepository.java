package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.VehicleLog;
import com.prmplatform.parqhub.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleLogRepository extends JpaRepository<VehicleLog, Long> {
    long countByExitTimeIsNull();
    Optional<VehicleLog> findByVehicleAndExitTimeIsNull(Vehicle vehicle);
    Optional<VehicleLog> findTopByVehicleAndExitTimeIsNotNullOrderByEntryTimeDesc(Vehicle vehicle);
    Optional<VehicleLog> findByVehicleAndExitTimeIsNotNull(Vehicle vehicle);
    Optional<VehicleLog> findByVehicleIdAndExitTimeIsNotNull(Long vehicleId);
}
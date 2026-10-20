package com.prmplatform.parqhub.repository;

import com.prmplatform.parqhub.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByUserId(Long userId);
    Optional<Vehicle> findByVehicleNo(String vehicleNo);
    boolean existsByVehicleNo(String vehicleNo);

    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.vehicleNo) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.model) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.color) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Vehicle> findByVehicleNoContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrColorContainingIgnoreCase(@Param("search") String search, @Param("search") String search2, @Param("search") String search3, @Param("search") String search4, Pageable pageable);


}

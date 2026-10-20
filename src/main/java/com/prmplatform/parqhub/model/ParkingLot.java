package com.prmplatform.parqhub.model;

import jakarta.persistence.*;
import java.util.List;
import java.math.BigDecimal;

@Entity
@Table(name = "parkinglot")
public class ParkingLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lot_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots;

    @Column(name = "price_hr", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceHr;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ParkingSlot> parkingSlots;

    // No-arg constructor
    public ParkingLot() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getTotalSlots() { return totalSlots; }
    public void setTotalSlots(Integer totalSlots) { this.totalSlots = totalSlots; }

    public BigDecimal getPriceHr() { return priceHr; }
    public void setPriceHr(BigDecimal priceHr) { this.priceHr = priceHr; }

    public List<ParkingSlot> getParkingSlots() { return parkingSlots; }
    public void setParkingSlots(List<ParkingSlot> parkingSlots) { this.parkingSlots = parkingSlots; }
}

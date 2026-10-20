package com.prmplatform.parqhub.service;

import com.prmplatform.parqhub.model.ParkingLot;
import com.prmplatform.parqhub.model.ParkingSlot;
import com.prmplatform.parqhub.repository.ParkingLotRepository;
import com.prmplatform.parqhub.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingViewerService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    public ParkingViewerService(ParkingLotRepository parkingLotRepository,
                                ParkingSlotRepository parkingSlotRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
    }

    /**
     * Get all unique cities (sorted)
     */
    public List<String> getAllCities() {
        return parkingLotRepository.findDistinctCities();
    }

    /**
     * Get unique locations for a city (sorted)
     */
    public List<String> getLocationsByCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return parkingLotRepository.findDistinctLocationsByCity(city);
    }

    /**
     * Get parking lot by city and location (first match)
     */
    public Optional<ParkingLot> getParkingLotByCityAndLocation(String city, String location) {
        if (city == null || location == null || city.trim().isEmpty() || location.trim().isEmpty()) {
            return Optional.empty();
        }
        return parkingLotRepository.findFirstByCityIgnoreCaseAndLocationIgnoreCase(city, location);
    }

    /**
     * Get parking lot by ID
     */
    public Optional<ParkingLot> getParkingLotById(Long lotId) {
        if (lotId == null || lotId <= 0) {
            return Optional.empty();
        }
        return parkingLotRepository.findById(lotId);
    }

    /**
     * Get all parking slots for a specific parking lot
     */
    public List<ParkingSlot> getParkingSlotsByLotId(Long lotId) {
        if (lotId == null || lotId <= 0) {
            return new ArrayList<>();
        }

        Optional<ParkingLot> lotOpt = parkingLotRepository.findById(lotId);
        if (lotOpt.isEmpty()) {
            return new ArrayList<>();
        }

        // Fetch all parking slots for the given parking lot
        return parkingSlotRepository.findByParkingLotId(lotId);
    }

    /**
     * Get parking slot statistics for a specific lot
     */
    public ParkingSlotStatistics getParkingSlotStatistics(Long lotId) {
        if (lotId == null || lotId <= 0) {
            return new ParkingSlotStatistics(0, 0, 0, 0);
        }

        Optional<ParkingLot> lotOpt = parkingLotRepository.findById(lotId);
        if (lotOpt.isEmpty()) {
            return new ParkingSlotStatistics(0, 0, 0, 0);
        }

        ParkingLot lot = lotOpt.get();
        long totalSlots = lot.getTotalSlots();

        long availableCount = parkingSlotRepository.countByParkingLotIdAndStatus(lotId, ParkingSlot.SlotStatus.AVAILABLE);
        long bookedCount = parkingSlotRepository.countByParkingLotIdAndStatus(lotId, ParkingSlot.SlotStatus.BOOKED);
        long occupiedCount = parkingSlotRepository.countByParkingLotIdAndStatus(lotId, ParkingSlot.SlotStatus.OCCUPIED);

        return new ParkingSlotStatistics(availableCount, bookedCount, occupiedCount, totalSlots);
    }

    /**
     * Get parking lot with its slots and statistics
     */
    public ParkingLotView getParkingLotView(Long lotId) {
        if (lotId == null || lotId <= 0) {
            return null;
        }

        Optional<ParkingLot> lotOpt = getParkingLotById(lotId);
        if (lotOpt.isEmpty()) {
            return null;
        }

        ParkingLot lot = lotOpt.get();
        List<ParkingSlot> slots = getParkingSlotsByLotId(lotId);
        ParkingSlotStatistics statistics = getParkingSlotStatistics(lotId);

        return new ParkingLotView(lot, slots, statistics);
    }

    /**
     * Check if parking lot exists
     */
    public boolean parkingLotExists(Long lotId) {
        if (lotId == null || lotId <= 0) {
            return false;
        }
        return parkingLotRepository.existsById(lotId);
    }

    /**
     * Inner class for parking slot statistics
     */
    public static class ParkingSlotStatistics {
        private final long availableCount;
        private final long bookedCount;
        private final long occupiedCount;
        private final long totalCount;

        public ParkingSlotStatistics(long availableCount, long bookedCount, long occupiedCount, long totalCount) {
            this.availableCount = availableCount;
            this.bookedCount = bookedCount;
            this.occupiedCount = occupiedCount;
            this.totalCount = totalCount;
        }

        public long getAvailableCount() { return availableCount; }
        public long getBookedCount() { return bookedCount; }
        public long getOccupiedCount() { return occupiedCount; }
        public long getTotalCount() { return totalCount; }

        public double getAvailablePercentage() {
            return totalCount > 0 ? (double) availableCount / totalCount * 100 : 0;
        }

        public double getBookedPercentage() {
            return totalCount > 0 ? (double) bookedCount / totalCount * 100 : 0;
        }

        public double getOccupiedPercentage() {
            return totalCount > 0 ? (double) occupiedCount / totalCount * 100 : 0;
        }
    }

    /**
     * Inner class for complete parking lot view
     */
    public static class ParkingLotView {
        private final ParkingLot parkingLot;
        private final List<ParkingSlot> parkingSlots;
        private final ParkingSlotStatistics statistics;

        public ParkingLotView(ParkingLot parkingLot, List<ParkingSlot> parkingSlots, ParkingSlotStatistics statistics) {
            this.parkingLot = parkingLot;
            this.parkingSlots = parkingSlots;
            this.statistics = statistics;
        }

        public ParkingLot getParkingLot() { return parkingLot; }
        public List<ParkingSlot> getParkingSlots() { return parkingSlots; }
        public ParkingSlotStatistics getStatistics() { return statistics; }
    }
}
package com.prmplatform.parqhub.service;

import com.prmplatform.parqhub.model.ParkingLot;
import com.prmplatform.parqhub.model.ParkingSlot;
import com.prmplatform.parqhub.repository.ParkingLotRepository;
import com.prmplatform.parqhub.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository,
                             ParkingSlotRepository parkingSlotRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
    }

    /**
     * Create parking slots for a parking lot based on the total slots count
     * @param parkingLot The parking lot for which slots need to be created
     */
    @Transactional
    public void createParkingSlotsForLot(ParkingLot parkingLot) {
        int totalSlots = parkingLot.getTotalSlots();
        
        // Delete any existing slots for this lot (if any)
        deleteParkingSlotsForLot(parkingLot.getId());
        
        // Create new slots, all with "Available" status
        for (int i = 0; i < totalSlots; i++) {
            ParkingSlot slot = new ParkingSlot();
            slot.setParkingLot(parkingLot);
            slot.setStatus(ParkingSlot.SlotStatus.AVAILABLE); // All new slots are available
            parkingSlotRepository.save(slot);
        }
    }

    /**
     * Update parking slots for a parking lot when the total slots count changes
     * @param parkingLot The parking lot for which slots need to be updated
     * @param oldTotalSlots The previous total slots count
     */
    @Transactional
    public void updateParkingSlotsForLot(ParkingLot parkingLot, int oldTotalSlots) {
        int newTotalSlots = parkingLot.getTotalSlots();
        
        if (newTotalSlots > oldTotalSlots) {
            // Need to add more slots
            int slotsToAdd = newTotalSlots - oldTotalSlots;
            for (int i = 0; i < slotsToAdd; i++) {
                ParkingSlot slot = new ParkingSlot();
                slot.setParkingLot(parkingLot);
                slot.setStatus(ParkingSlot.SlotStatus.AVAILABLE); // All new slots are available
                parkingSlotRepository.save(slot);
            }
        } else if (newTotalSlots < oldTotalSlots) {
            // Need to remove excess slots
            List<ParkingSlot> slots = parkingSlotRepository.findByParkingLotId(parkingLot.getId());
            int slotsToRemove = oldTotalSlots - newTotalSlots;
            
            // Remove from the end
            for (int i = 0; i < slotsToRemove && i < slots.size(); i++) {
                parkingSlotRepository.delete(slots.get(slots.size() - 1 - i));
            }
        }
        // If equal, no action needed
    }

    /**
     * Delete all parking slots for a parking lot
     * @param lotId The ID of the parking lot
     */
    @Transactional
    public void deleteParkingSlotsForLot(Long lotId) {
        List<ParkingSlot> slots = parkingSlotRepository.findByParkingLotId(lotId);
        parkingSlotRepository.deleteAll(slots);
    }
}
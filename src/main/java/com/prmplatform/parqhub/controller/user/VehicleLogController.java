package com.prmplatform.parqhub.controller.user;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.User;
import com.prmplatform.parqhub.model.Vehicle;
import com.prmplatform.parqhub.model.ParkingLot;
import com.prmplatform.parqhub.model.VehicleLog;
import com.prmplatform.parqhub.model.ParkingSlot;
import com.prmplatform.parqhub.repository.BookingRepository;
import com.prmplatform.parqhub.repository.VehicleLogRepository;
import com.prmplatform.parqhub.repository.ParkingSlotRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class VehicleLogController {

    @Autowired
    private VehicleLogRepository vehicleLogRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @PostMapping("/log-entry")
    @ResponseBody
    public Map<String, String> logEntry(@RequestParam Long bookingId, @RequestParam Long vehicleId, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        try {
            LocalDateTime now = LocalDateTime.now();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not logged in");
                return response;
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // Validate user ownership
            if (!booking.getUser().getId().equals(user.getId())) {
                response.put("status", "error");
                response.put("message", "Unauthorized access to booking");
                return response;
            }

            // Validate payment status
            if (!booking.getPaymentStatus().name().equals("Pending")) {
                response.put("status", "error");
                response.put("message", "Cannot log entry for non-pending booking");
                return response;
            }

            // Validate vehicle
            if (!booking.getVehicle().getId().equals(vehicleId)) {
                response.put("status", "error");
                response.put("message", "Vehicle does not match booking");
                return response;
            }

            // Check if booking has a valid parking slot
            if (booking.getParkingSlot() == null) {
                response.put("status", "error");
                response.put("message", "Booking has no assigned parking slot");
                return response;
            }

            Vehicle vehicle = booking.getVehicle();
            ParkingLot parkingLot = booking.getParkingSlot().getParkingLot();

            // Validate parking lot exists and has a valid ID
            if (parkingLot == null || parkingLot.getId() == null) {
                response.put("status", "error");
                response.put("message", "Invalid parking lot reference");
                return response;
            }

            // Check for existing active log for this vehicle in the same parking lot
            Optional<VehicleLog> existingLog = vehicleLogRepository.findByVehicleAndExitTimeIsNull(vehicle);
            if (existingLog.isPresent()) {
                VehicleLog log = existingLog.get();
                if (log.getParkingLot().getId().equals(parkingLot.getId())) {
                    response.put("status", "error");
                    response.put("message", "Vehicle already has an active log entry in this parking lot");
                    return response;
                }
            }

            // Update parking slot status to OCCUPIED
            ParkingSlot parkingSlot = booking.getParkingSlot();
            parkingSlot.setStatus(ParkingSlot.SlotStatus.OCCUPIED);
            parkingSlotRepository.save(parkingSlot);

            // Create VehicleLog with proper entity relationships
            VehicleLog log = new VehicleLog();
            log.setVehicle(vehicle);
            log.setEntryTime(now);
            log.setParkingLot(parkingLot);

            VehicleLog savedLog = vehicleLogRepository.save(log);

            response.put("status", "success");
            response.put("message", "Entry logged successfully");
            response.put("entryTime", savedLog.getEntryTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to log entry: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/log-exit")
    @ResponseBody
    public Map<String, String> logExit(@RequestParam Long bookingId, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        try {
            LocalDateTime now = LocalDateTime.now();
            User user = (User) session.getAttribute("loggedInUser");

            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not logged in");
                return response;
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            if (!booking.getUser().getId().equals(user.getId())) {
                response.put("status", "error");
                response.put("message", "Unauthorized access to booking");
                return response;
            }

            if (!booking.getPaymentStatus().name().equals("Pending")) {
                response.put("status", "error");
                response.put("message", "Cannot log exit for non-pending booking");
                return response;
            }

            // Find the VehicleLog for this vehicle and parking lot with no exit time
            Vehicle vehicle = booking.getVehicle();
            ParkingLot parkingLot = booking.getParkingSlot().getParkingLot();
            Optional<VehicleLog> logOptional = vehicleLogRepository.findByVehicleAndExitTimeIsNull(vehicle);
            if (!logOptional.isPresent() || !logOptional.get().getParkingLot().getId().equals(parkingLot.getId())) {
                response.put("status", "error");
                response.put("message", "No active entry found for this vehicle in this parking lot");
                return response;
            }

            VehicleLog log = logOptional.get();
            log.setExitTime(now);
            VehicleLog savedLog = vehicleLogRepository.save(log);

            // Update parking slot status to AVAILABLE
            ParkingSlot parkingSlot = booking.getParkingSlot();
            parkingSlot.setStatus(ParkingSlot.SlotStatus.AVAILABLE);
            parkingSlotRepository.save(parkingSlot);

            response.put("status", "success");
            response.put("message", "Exit logged successfully");
            response.put("redirectUrl", "/user/payment-gateway?bookingId=" + bookingId);
            response.put("exitTime", savedLog.getExitTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to log exit: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/log-entry-time")
    @ResponseBody
    public Map<String, String> getLogEntryTime(@RequestParam Long bookingId, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not logged in");
                return response;
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            if (!booking.getUser().getId().equals(user.getId())) {
                response.put("status", "error");
                response.put("message", "Unauthorized access to booking");
                return response;
            }

            Vehicle vehicle = booking.getVehicle();
            ParkingLot parkingLot = booking.getParkingSlot().getParkingLot();
            Optional<VehicleLog> logOptional = vehicleLogRepository.findByVehicleAndExitTimeIsNull(vehicle);
            if (!logOptional.isPresent() || !logOptional.get().getParkingLot().getId().equals(parkingLot.getId())) {
                response.put("status", "error");
                response.put("message", "No active entry found for this vehicle in this parking lot");
                return response;
            }

            VehicleLog log = logOptional.get();
            response.put("status", "success");
            response.put("entryTime", log.getEntryTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve entry time: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/log-exit-time")
    @ResponseBody
    public Map<String, String> getLogExitTime(@RequestParam Long bookingId, HttpSession session) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) {
                response.put("status", "error");
                response.put("message", "User not logged in");
                return response;
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            if (!booking.getUser().getId().equals(user.getId())) {
                response.put("status", "error");
                response.put("message", "Unauthorized access to booking");
                return response;
            }

            Vehicle vehicle = booking.getVehicle();
            ParkingLot parkingLot = booking.getParkingSlot().getParkingLot();
            Optional<VehicleLog> logOptional = vehicleLogRepository.findTopByVehicleAndExitTimeIsNotNullOrderByEntryTimeDesc(vehicle);
            if (!logOptional.isPresent() || !logOptional.get().getParkingLot().getId().equals(parkingLot.getId())) {
                response.put("status", "error");
                response.put("message", "No exit logged for this vehicle in this parking lot");
                return response;
            }

            VehicleLog log = logOptional.get();
            response.put("status", "success");
            response.put("exitTime", log.getExitTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            return response;
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to retrieve exit time: " + e.getMessage());
            return response;
        }
    }
}
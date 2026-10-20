package com.prmplatform.parqhub.controller.admin;

import com.prmplatform.parqhub.model.*;
import com.prmplatform.parqhub.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminManageSlotController {

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping("/manage-parking-slot")
    public String manageParkingSlot(@RequestParam Long slotId, HttpSession session, Model model) {
        try {
            Admin admin = (Admin) session.getAttribute("loggedInAdmin");
            if (admin == null) {
                return "redirect:/admin/login";
            }

            model.addAttribute("adminName", admin.getName());
            model.addAttribute("adminRole", admin.getRole());

            Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(slotId);
            if (slotOpt.isPresent()) {
                ParkingSlot slot = slotOpt.get();
                model.addAttribute("slot", slot);
                
                // Get the active booking for this slot if it's booked or occupied
                if (slot.getStatus() == ParkingSlot.SlotStatus.BOOKED || 
                    slot.getStatus() == ParkingSlot.SlotStatus.OCCUPIED) {
                    
                    List<Booking> bookings = bookingRepository.findByParkingSlotId(slotId);
                    if (!bookings.isEmpty()) {
                        // Find the active booking
                        Booking activeBooking = null;
                        for (Booking booking : bookings) {
                            if (booking.getEndTime() == null || booking.getEndTime().isAfter(LocalDateTime.now())) {
                                activeBooking = booking;
                                break;
                            }
                        }
                        
                        if (activeBooking != null) {
                            model.addAttribute("booking", activeBooking);
                            model.addAttribute("customer", activeBooking.getUser());
                            model.addAttribute("vehicle", activeBooking.getVehicle());
                            // Fetch notifications for the user
                            List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(activeBooking.getUser().getId());
                            model.addAttribute("notifications", notifications);
                        }
                    }
                }
            } else {
                return "redirect:/admin/parking-viewer";
            }

            return "admin/manageParkingSlot";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/parking-viewer";
        }
    }

    @GetMapping("/notifications")
    @ResponseBody
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestParam Long slotId, HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("loggedInAdmin");
            if (admin == null) {
                return ResponseEntity.status(401).body(null);
            }

            Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(slotId);
            if (!slotOpt.isPresent()) {
                return ResponseEntity.badRequest().body(null);
            }

            // Get the active booking for this slot
            List<Booking> bookings = bookingRepository.findByParkingSlotId(slotId);
            Booking activeBooking = null;
            for (Booking booking : bookings) {
                if (booking.getEndTime() == null || booking.getEndTime().isAfter(LocalDateTime.now())) {
                    activeBooking = booking;
                    break;
                }
            }

            if (activeBooking == null) {
                return ResponseEntity.ok(List.of());
            }

            // Fetch notifications for the user
            List<Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(activeBooking.getUser().getId());
            
            // Convert to DTO for JSON response
            List<NotificationDTO> notificationDTOs = notifications.stream().map(notification -> {
                NotificationDTO dto = new NotificationDTO();
                dto.setType(notification.getType().toString());
                dto.setDescription(notification.getDescription());
                dto.setTimestamp(notification.getTimestamp());
                dto.setAdminName(notification.getAdmin() != null ? notification.getAdmin().getName() : "N/A");
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/send-notification")
    @ResponseBody
    public ResponseEntity<String> sendNotification(
            @RequestParam Long slotId,
            @RequestParam String notificationType,
            @RequestParam String description,
            HttpSession session) {
        
        try {
            Admin admin = (Admin) session.getAttribute("loggedInAdmin");
            if (admin == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(slotId);
            if (!slotOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Invalid slot ID");
            }

            ParkingSlot slot = slotOpt.get();
            
            // Get the active booking for this slot
            List<Booking> bookings = bookingRepository.findByParkingSlotId(slotId);
            Booking activeBooking = null;
            for (Booking booking : bookings) {
                if (booking.getEndTime() == null || booking.getEndTime().isAfter(LocalDateTime.now())) {
                    activeBooking = booking;
                    break;
                }
            }

            if (activeBooking == null) {
                return ResponseEntity.badRequest().body("No active booking found for this slot");
            }

            // Validate notification type
            try {
                Notification.NotificationType.valueOf(notificationType);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid notification type");
            }

            // Create notification
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.valueOf(notificationType));
            notification.setDescription(description);
            notification.setTimestamp(LocalDateTime.now());
            notification.setUser(activeBooking.getUser());
            notification.setAdmin(admin);

            notificationRepository.save(notification);

            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while sending notification");
        }
    }

    // DTO class for notifications
    public static class NotificationDTO {
        private String type;
        private String description;
        private LocalDateTime timestamp;
        private String adminName;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getAdminName() { return adminName; }
        public void setAdminName(String adminName) { this.adminName = adminName; }
    }
}
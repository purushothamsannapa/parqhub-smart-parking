package com.prmplatform.parqhub.controller.admin;

import com.prmplatform.parqhub.model.Admin;
import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.Notification;
import com.prmplatform.parqhub.model.ParkingSlot.SlotStatus;
import com.prmplatform.parqhub.repository.*;
import com.prmplatform.parqhub.service.AdminAuthenticationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminRepository adminRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final VehicleLogRepository vehicleLogRepository;
    private final UserRepository userRepository;
    private final AdminAuthenticationService adminAuthenticationService;

    @Autowired
    public AdminController(AdminRepository adminRepository, ParkingSlotRepository parkingSlotRepository,
                           PaymentRepository paymentRepository, BookingRepository bookingRepository,
                           NotificationRepository notificationRepository, VehicleLogRepository vehicleLogRepository,
                           UserRepository userRepository, AdminAuthenticationService adminAuthenticationService) {
        this.adminRepository = adminRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
        this.vehicleLogRepository = vehicleLogRepository;
        this.userRepository = userRepository;
        this.adminAuthenticationService = adminAuthenticationService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "admin-login";
    }
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }


    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model,
                        HttpSession session) {
        return adminRepository.findByEmailAndPassword(email, password)
                .map(admin -> {
                    session.setAttribute("loggedInAdmin", admin);
                    return "redirect:/admin/dashboard";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Invalid email or password");
                    return "admin-login";
                });
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        // Authenticate admin using strategy pattern
        if (!adminAuthenticationService.authenticateAdmin(admin)) {
            session.invalidate();
            model.addAttribute("error", "Unauthorized access");
            return "admin-login";
        }

        model.addAttribute("adminName", admin.getName());
        model.addAttribute("adminRole", admin.getRole());

        try {
            if (adminAuthenticationService.hasAccessToSection(admin, "operations")) {
                List<Object[]> slotSummaries = parkingSlotRepository.countByCityAndStatus(SlotStatus.AVAILABLE);
                Map<String, Long> availableSlotsByCity = new HashMap<>();
                for (Object[] result : slotSummaries) {
                    availableSlotsByCity.put((String) result[0], (Long) result[1]);
                }
                model.addAttribute("availableSlotsByCity", availableSlotsByCity);
                model.addAttribute("totalActiveSlots", parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED));
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "finance")) {
                LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
                Double todayRevenue = paymentRepository.sumAmountByCompletedAndTimestampAfter(startOfDay);
                model.addAttribute("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);
                model.addAttribute("pendingPayments", bookingRepository.countByPaymentStatus(Booking.PaymentStatus.Pending));
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "customer")) {
                model.addAttribute("totalUsers", userRepository.count());
                model.addAttribute("pendingBookings", bookingRepository.countByPaymentStatus(Booking.PaymentStatus.Pending));
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "security")) {
                model.addAttribute("activeVehicles", vehicleLogRepository.countByExitTimeIsNull());
                model.addAttribute("securityIncidents", notificationRepository.countByType(Notification.NotificationType.SECURITY_INCIDENT));
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "it")) {
                model.addAttribute("systemUptime", "99.9%"); // Placeholder
                model.addAttribute("errorLogs", notificationRepository.countByType(Notification.NotificationType.NONE));
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching dashboard data: " + e.getMessage());
        }

        return "admin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    /**
     * API endpoint for dashboard live data
     */
    @GetMapping("/api/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Admin not logged in"));
        }

        // Authenticate admin using strategy pattern
        if (!adminAuthenticationService.authenticateAdmin(admin)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access"));
        }

        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Common stats for all roles
            stats.put("adminName", admin.getName());
            stats.put("adminRole", admin.getRole().name());
            stats.put("timestamp", LocalDateTime.now());
            
            // Role-specific stats using strategy pattern
            if (adminAuthenticationService.hasAccessToSection(admin, "operations")) {
                List<Object[]> slotSummaries = parkingSlotRepository.countByCityAndStatus(SlotStatus.AVAILABLE);
                Map<String, Long> availableSlotsByCity = new HashMap<>();
                for (Object[] result : slotSummaries) {
                    availableSlotsByCity.put((String) result[0], (Long) result[1]);
                }
                stats.put("availableSlotsByCity", availableSlotsByCity);
                stats.put("totalActiveSlots", parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED));
                stats.put("totalAvailableSlots", parkingSlotRepository.countByStatus(SlotStatus.AVAILABLE));
                stats.put("totalSlots", parkingSlotRepository.count());
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "finance")) {
                LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
                Double todayRevenue = paymentRepository.sumAmountByCompletedAndTimestampAfter(startOfDay);
                stats.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);
                stats.put("totalRevenue", paymentRepository.getTotalCompletedRevenue());
                stats.put("pendingPayments", bookingRepository.countByPaymentStatus(Booking.PaymentStatus.Pending));
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "customer")) {
                stats.put("totalUsers", userRepository.count());
                stats.put("pendingBookings", bookingRepository.countByPaymentStatus(Booking.PaymentStatus.Pending));
                stats.put("completedBookings", bookingRepository.countByPaymentStatus(Booking.PaymentStatus.Completed));
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "security")) {
                stats.put("activeVehicles", vehicleLogRepository.countByExitTimeIsNull());
                stats.put("securityIncidents", notificationRepository.countByType(Notification.NotificationType.SECURITY_INCIDENT));
                stats.put("totalVehicleLogs", vehicleLogRepository.count());
            }

            if (adminAuthenticationService.hasAccessToSection(admin, "it")) {
                stats.put("systemUptime", "99.9%");
                stats.put("errorLogs", notificationRepository.countByType(Notification.NotificationType.NONE));
                stats.put("totalNotifications", notificationRepository.count());
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            stats.put("error", "Error fetching dashboard data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(stats);
        }
    }

    /**
     * API endpoint for real-time system health
     */
    @GetMapping("/api/dashboard/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSystemHealth(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Admin not logged in"));
        }

        Map<String, Object> health = new HashMap<>();
        
        try {
            // System performance metrics
            long totalBookings = bookingRepository.count();
            long completedBookings = bookingRepository.countByPaymentStatus(Booking.PaymentStatus.Completed);
            double bookingSuccessRate = totalBookings > 0 ? (double) completedBookings / totalBookings * 100 : 0;
            
            health.put("bookingSuccessRate", Math.round(bookingSuccessRate * 100.0) / 100.0);
            health.put("totalBookings", totalBookings);
            health.put("systemUptime", "99.9%");
            health.put("avgResponseTime", "120");
            health.put("errorRate", "0.1");
            health.put("timestamp", LocalDateTime.now());
            
            // Occupancy rate
            long totalSlots = parkingSlotRepository.count();
            long occupiedSlots = parkingSlotRepository.countByStatus(SlotStatus.OCCUPIED);
            double occupancyRate = totalSlots > 0 ? (double) occupiedSlots / totalSlots * 100 : 0;
            health.put("currentOccupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("error", "Error fetching system health: " + e.getMessage());
            return ResponseEntity.internalServerError().body(health);
        }
    }


}
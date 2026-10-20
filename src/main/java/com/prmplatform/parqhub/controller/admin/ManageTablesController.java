package com.prmplatform.parqhub.controller.admin;

import com.prmplatform.parqhub.model.*;
import com.prmplatform.parqhub.repository.*;
import com.prmplatform.parqhub.service.ParkingLotService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/tables")
public class ManageTablesController {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final VehicleLogRepository vehicleLogRepository;
    private final ReportRepository reportRepository;
    private final ParkingLotService parkingLotService;

    public ManageTablesController(AdminRepository adminRepository, UserRepository userRepository,
                                  VehicleRepository vehicleRepository, ParkingLotRepository parkingLotRepository,
                                  ParkingSlotRepository parkingSlotRepository, BookingRepository bookingRepository,
                                  PaymentRepository paymentRepository, NotificationRepository notificationRepository,
                                  VehicleLogRepository vehicleLogRepository, ReportRepository reportRepository,
                                  ParkingLotService parkingLotService) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.notificationRepository = notificationRepository;
        this.vehicleLogRepository = vehicleLogRepository;
        this.reportRepository = reportRepository;
        this.parkingLotService = parkingLotService;
    }

    @GetMapping
    public String tables(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminName", admin.getName());
        model.addAttribute("adminRole", admin.getRole());

        return "admin/tables";
    }

    @GetMapping("/{tableName}")
    public String manageTable(@PathVariable String tableName,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(defaultValue = "id") String sortBy,
                              @RequestParam(defaultValue = "asc") String sortDir,
                              @RequestParam(defaultValue = "") String search,
                              HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminName", admin.getName());
        model.addAttribute("adminRole", admin.getRole());
        model.addAttribute("tableName", tableName);
        model.addAttribute("searchTerm", search);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<?> pageResult;

        switch (tableName) {
            case "admins":
                if (search.isEmpty()) {
                    pageResult = adminRepository.findAll(pageable);
                } else {
                    pageResult = adminRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable);
                }
                break;
            case "users":
                if (search.isEmpty()) {
                    pageResult = userRepository.findAll(pageable);
                } else {
                    pageResult = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrContactNoContainingIgnoreCase(search, search, search, pageable);
                }
                break;
            case "vehicle":
                if (search.isEmpty()) {
                    pageResult = vehicleRepository.findAll(pageable);
                } else {
                    pageResult = vehicleRepository.findByVehicleNoContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrColorContainingIgnoreCase(search, search, search, search, pageable);
                }
                break;
            case "parkinglot":
                if (search.isEmpty()) {
                    pageResult = parkingLotRepository.findAll(pageable);
                } else {
                    pageResult = parkingLotRepository.findByCityContainingIgnoreCaseOrLocationContainingIgnoreCase(search, search, pageable);
                }
                break;
            case "parkingslot":
                if (search.isEmpty()) {
                    pageResult = parkingSlotRepository.findAll(pageable);
                } else {
                    try {
                        ParkingSlot.SlotStatus status = ParkingSlot.SlotStatus.valueOf(search.toUpperCase());
                        pageResult = parkingSlotRepository.findByStatus(status, pageable);
                    } catch (IllegalArgumentException e) {
                        pageResult = parkingSlotRepository.findAll(pageable);
                    }
                }
                break;
            case "booking":
                if (search.isEmpty()) {
                    pageResult = bookingRepository.findAll(pageable);
                } else {
                    try {
                        Booking.PaymentStatus status = Booking.PaymentStatus.valueOf(search.toUpperCase());
                        pageResult = bookingRepository.findByPaymentStatus(status, pageable);
                    } catch (IllegalArgumentException e) {
                        pageResult = bookingRepository.findAll(pageable);
                    }
                }
                break;
            case "payment":
                if (search.isEmpty()) {
                    pageResult = paymentRepository.findAll(pageable);
                } else {
                    pageResult = paymentRepository.findByMethodContainingIgnoreCaseOrStatusContainingIgnoreCase(search, search, pageable);
                }
                break;
            case "notification":
                if (search.isEmpty()) {
                    pageResult = notificationRepository.findAll(pageable);
                } else {
                    pageResult = notificationRepository.findByDescriptionContainingIgnoreCaseOrTypeContainingIgnoreCase(search, search, pageable);
                }
                break;
            case "vehiclelog":
                if (search.isEmpty()) {
                    pageResult = vehicleLogRepository.findAll(pageable);
                } else {
                    pageResult = vehicleLogRepository.findAll(pageable); // Limited search due to datetime fields
                }
                break;
            case "report":
                if (search.isEmpty()) {
                    pageResult = reportRepository.findAll(pageable);
                } else {
                    try {
                        Report.ReportType type = Report.ReportType.valueOf(search.toUpperCase());
                        pageResult = reportRepository.findByType(type, pageable);
                    } catch (IllegalArgumentException e) {
                        pageResult = reportRepository.findAll(pageable);
                    }
                }
                break;
            default:
                return "redirect:/admin/tables";
        }

        model.addAttribute("items", pageResult.getContent());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("startItem", page * size + 1);
        model.addAttribute("endItem", Math.min((page + 1) * size, (int) pageResult.getTotalElements()));

        return "admin/manageTable";
    }

    @GetMapping("/{tableName}/{id}")
    @ResponseBody
    public ResponseEntity<?> getItem(@PathVariable String tableName, @PathVariable Long id) {
        try {
            switch (tableName) {
                case "admins":
                    return adminRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "users":
                    return userRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "vehicle":
                    return vehicleRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "parkinglot":
                    return parkingLotRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "parkingslot":
                    return parkingSlotRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "booking":
                    return bookingRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "payment":
                    return paymentRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "notification":
                    return notificationRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "vehiclelog":
                    return vehicleLogRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                case "report":
                    return reportRepository.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
                default:
                    return ResponseEntity.badRequest().body("Invalid table name");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching item: " + e.getMessage());
        }
    }

    @PostMapping("/{tableName}/save")
    public String saveItem(@PathVariable String tableName, @RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        try {
            switch (tableName) {
                case "admins":
                    Long adminId = getLongParam(params, "admin_id");
                    Admin admin = adminId == null ? new Admin() : adminRepository.findById(adminId).orElseThrow();
                    admin.setName(params.get("name"));
                    admin.setEmail(params.get("email"));
                    admin.setPassword(params.get("password"));
                    // Map display names to enum values
                    String roleDisplayName = params.get("role");
                    Admin.Role role = mapRoleDisplayNameToEnum(roleDisplayName);
                    admin.setRole(role);
                    adminRepository.save(admin);
                    break;
                case "users":
                    Long userId = getLongParam(params, "user_id");
                    User user = userId == null ? new User() : userRepository.findById(userId).orElseThrow();
                    user.setName(params.get("name"));
                    user.setEmail(params.get("email"));
                    user.setPassword(params.get("password"));
                    user.setContactNo(params.get("contact_no"));
                    userRepository.save(user);
                    break;
                case "vehicle":
                    Long vehicleId = getLongParam(params, "vehicle_id");
                    Vehicle vehicle = vehicleId == null ? new Vehicle() : vehicleRepository.findById(vehicleId).orElseThrow();
                    Long ownerId = getLongParam(params, "user_id");
                    vehicle.setUser(ownerId == null ? null : userRepository.findById(ownerId).orElseThrow());
                    vehicle.setVehicleNo(params.get("vehicle_no"));
                    vehicle.setVehicleType(Vehicle.VehicleType.valueOf(params.get("vehicle_type")));
                    vehicle.setBrand(params.get("brand"));
                    vehicle.setModel(params.get("model"));
                    vehicle.setColor(params.get("color"));
                    vehicleRepository.save(vehicle);
                    break;
                case "parkinglot":
                    Long lotId = getLongParam(params, "lot_id");
                    ParkingLot parkingLot = lotId == null ? new ParkingLot() : parkingLotRepository.findById(lotId).orElseThrow();
                    
                    // Store the old total slots count if this is an update
                    int oldTotalSlots = (parkingLot.getId() != null) ? parkingLot.getTotalSlots() : 0;
                    
                    parkingLot.setCity(params.get("city"));
                    parkingLot.setLocation(params.get("location"));
                    parkingLot.setTotalSlots(Integer.parseInt(params.get("total_slots")));
                    parkingLot.setPriceHr(new BigDecimal(params.get("price_hr")));
                    parkingLotRepository.save(parkingLot);
                    
                    // Handle parking slots creation/update
                    if (lotId == null) {
                        // This is a new parking lot, create slots
                        parkingLotService.createParkingSlotsForLot(parkingLot);
                    } else {
                        // This is an update, update slots if total slots count changed
                        if (parkingLot.getTotalSlots() != oldTotalSlots) {
                            parkingLotService.updateParkingSlotsForLot(parkingLot, oldTotalSlots);
                        }
                    }
                    break;
                case "parkingslot":
                    Long slotId = getLongParam(params, "slot_id");
                    ParkingSlot parkingSlot = slotId == null ? new ParkingSlot() : parkingSlotRepository.findById(slotId).orElseThrow();
                    Long parkingLotId = getLongParam(params, "lot_id");
                    parkingSlot.setParkingLot(parkingLotId == null ? null : parkingLotRepository.findById(parkingLotId).orElseThrow());
                    parkingSlot.setStatus(ParkingSlot.SlotStatus.valueOf(params.get("status")));
                    parkingSlotRepository.save(parkingSlot);
                    break;
                case "booking":
                    Long bookingId = getLongParam(params, "booking_id");
                    Booking booking = bookingId == null ? new Booking() : bookingRepository.findById(bookingId).orElseThrow();
                    Long bookingUserId = getLongParam(params, "user_id");
                    booking.setUser(bookingUserId == null ? null : userRepository.findById(bookingUserId).orElseThrow());
                    Long bookingVehicleId = getLongParam(params, "vehicle_id");
                    booking.setVehicle(bookingVehicleId == null ? null : vehicleRepository.findById(bookingVehicleId).orElseThrow());
                    Long bookingSlotId = getLongParam(params, "slot_id");
                    booking.setParkingSlot(bookingSlotId == null ? null : parkingSlotRepository.findById(bookingSlotId).orElseThrow());
                    booking.setStartTime(LocalDateTime.parse(params.get("start_time")));
                    String endTimeStr = params.get("end_time");
                    booking.setEndTime(endTimeStr.isEmpty() ? null : LocalDateTime.parse(endTimeStr));
                    booking.setPaymentStatus(Booking.PaymentStatus.valueOf(params.get("payment_status")));
                    bookingRepository.save(booking);
                    break;
                case "payment":
                    Long paymentId = getLongParam(params, "payment_id");
                    Payment payment = paymentId == null ? new Payment() : paymentRepository.findById(paymentId).orElseThrow();
                    Long paymentBookingId = getLongParam(params, "booking_id");
                    payment.setBooking(paymentBookingId == null ? null : bookingRepository.findById(paymentBookingId).orElseThrow());
                    payment.setAmount(new BigDecimal(params.get("amount")));
                    payment.setMethod(Payment.PaymentMethod.valueOf(params.get("method")));
                    payment.setStatus(Payment.PaymentStatus.valueOf(params.get("status")));
                    payment.setTimestamp(LocalDateTime.now());
                    paymentRepository.save(payment);
                    break;
                case "notification":
                    Long notificationId = getLongParam(params, "notification_id");
                    Notification notification = notificationId == null ? new Notification() : notificationRepository.findById(notificationId).orElseThrow();
                    notification.setType(Notification.NotificationType.valueOf(params.get("type")));
                    notification.setDescription(params.get("description"));
                    Long notificationUserId = getLongParam(params, "user_id");
                    notification.setUser(notificationUserId == null ? null : userRepository.findById(notificationUserId).orElseThrow());
                    Long notificationAdminId = getLongParam(params, "admin_id");
                    notification.setAdmin(notificationAdminId == null ? null : adminRepository.findById(notificationAdminId).orElseThrow());
                    notification.setTimestamp(LocalDateTime.now());
                    notificationRepository.save(notification);
                    break;
                case "vehiclelog":
                    Long logId = getLongParam(params, "log_id");
                    VehicleLog vehicleLog = logId == null ? new VehicleLog() : vehicleLogRepository.findById(logId).orElseThrow();
                    Long logVehicleId = getLongParam(params, "vehicle_id");
                    vehicleLog.setVehicle(logVehicleId == null ? null : vehicleRepository.findById(logVehicleId).orElseThrow());
                    Long logLotId = getLongParam(params, "lot_id");
                    vehicleLog.setParkingLot(logLotId == null ? null : parkingLotRepository.findById(logLotId).orElseThrow());
                    vehicleLog.setEntryTime(LocalDateTime.parse(params.get("entry_time")));
                    String exitTimeStr = params.get("exit_time");
                    vehicleLog.setExitTime(exitTimeStr.isEmpty() ? null : LocalDateTime.parse(exitTimeStr));
                    vehicleLogRepository.save(vehicleLog);
                    break;
                case "report":
                    Long reportId = getLongParam(params, "report_id");
                    Report report = reportId == null ? new Report() : reportRepository.findById(reportId).orElseThrow();
                    report.setType(Report.ReportType.valueOf(params.get("type")));
                    Long reportAdminId = getLongParam(params, "admin_id");
                    report.setAdmin(reportAdminId == null ? null : adminRepository.findById(reportAdminId).orElseThrow());
                    report.setGeneratedDate(LocalDateTime.now());
                    reportRepository.save(report);
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error", "Invalid table name");
                    return "redirect:/admin/tables";
            }
            redirectAttributes.addFlashAttribute("success", "Item saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving item: " + e.getMessage());
        }
        return "redirect:/admin/tables/" + tableName;
    }

    private Long getLongParam(Map<String, String> params, String key) {
        String value = params.get(key);
        return (value == null || value.isBlank()) ? null : Long.parseLong(value);
    }

    /**
     * Maps display names to enum values for admin roles
     * @param displayName The display name from the form
     * @return The corresponding enum value
     */
    private Admin.Role mapRoleDisplayNameToEnum(String displayName) {
        if (displayName == null) {
            return Admin.Role.SUPER_ADMIN; // Default role
        }
        
        switch (displayName) {
            case "Operations Manager":
                return Admin.Role.OPERATIONS_MANAGER;
            case "Finance Officer":
                return Admin.Role.FINANCE_OFFICER;
            case "Security Supervisor":
                return Admin.Role.SECURITY_SUPERVISOR;
            case "IT Support":
                return Admin.Role.IT_SUPPORT;
            case "Customer Service Officer":
                return Admin.Role.CUSTOMER_SERVICE_OFFICER;
            case "Super Admin":
                return Admin.Role.SUPER_ADMIN;
            default:
                // Try to parse as enum directly (in case it's already in enum format)
                try {
                    return Admin.Role.valueOf(displayName);
                } catch (IllegalArgumentException e) {
                    return Admin.Role.SUPER_ADMIN; // Default role if parsing fails
                }
        }
    }

    @DeleteMapping("/{tableName}/{id}")
    @ResponseBody
    public String deleteItem(@PathVariable String tableName, @PathVariable Long id) {
        try {
            switch (tableName) {
                case "admins":
                    if (!adminRepository.existsById(id)) return "error: Admin not found";
                    adminRepository.deleteById(id);
                    break;
                case "users":
                    if (!userRepository.existsById(id)) return "error: User not found";
                    userRepository.deleteById(id);
                    break;
                case "vehicle":
                    if (!vehicleRepository.existsById(id)) return "error: Vehicle not found";
                    vehicleRepository.deleteById(id);
                    break;
                case "parkinglot":
                    if (!parkingLotRepository.existsById(id)) return "error: Parking lot not found";
                    // Delete associated parking slots first
                    parkingLotService.deleteParkingSlotsForLot(id);
                    parkingLotRepository.deleteById(id);
                    break;
                case "parkingslot":
                    if (!parkingSlotRepository.existsById(id)) return "error: Parking slot not found";
                    parkingSlotRepository.deleteById(id);
                    break;
                case "booking":
                    if (!bookingRepository.existsById(id)) return "error: Booking not found";
                    bookingRepository.deleteById(id);
                    break;
                case "payment":
                    if (!paymentRepository.existsById(id)) return "error: Payment not found";
                    paymentRepository.deleteById(id);
                    break;
                case "notification":
                    if (!notificationRepository.existsById(id)) return "error: Notification not found";
                    notificationRepository.deleteById(id);
                    break;
                case "vehiclelog":
                    if (!vehicleLogRepository.existsById(id)) return "error: Vehicle log not found";
                    vehicleLogRepository.deleteById(id);
                    break;
                case "report":
                    if (!reportRepository.existsById(id)) return "error: Report not found";
                    reportRepository.deleteById(id);
                    break;
                default:
                    return "error: Invalid table name";
            }
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
package com.prmplatform.parqhub.controller.user;

import com.prmplatform.parqhub.model.Booking;
import com.prmplatform.parqhub.model.ParkingLot;
import com.prmplatform.parqhub.model.ParkingSlot;
import com.prmplatform.parqhub.model.Payment;
import com.prmplatform.parqhub.model.User;
import com.prmplatform.parqhub.model.VehicleLog;
import com.prmplatform.parqhub.repository.BookingRepository;
import com.prmplatform.parqhub.repository.ParkingLotRepository;
import com.prmplatform.parqhub.repository.ParkingSlotRepository;
import com.prmplatform.parqhub.repository.PaymentRepository;
import com.prmplatform.parqhub.repository.VehicleLogRepository;
import com.prmplatform.parqhub.service.PaymentProcessingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final VehicleLogRepository vehicleLogRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final JavaMailSender mailSender;
    private final PaymentProcessingService paymentProcessingService;

    @Value("${spring.mail.from:parqhub.system@gmail.com}")
    private String fromEmail;

    @Autowired
    public PaymentController(BookingRepository bookingRepository,
                             PaymentRepository paymentRepository,
                             VehicleLogRepository vehicleLogRepository,
                             ParkingLotRepository parkingLotRepository,
                             ParkingSlotRepository parkingSlotRepository,
                             @Autowired(required = false) JavaMailSender mailSender,
                             PaymentProcessingService paymentProcessingService) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.vehicleLogRepository = vehicleLogRepository;
        this.parkingLotRepository = parkingLotRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.mailSender = mailSender;
        this.paymentProcessingService = paymentProcessingService;
    }

    @GetMapping("/payment-gateway")
    public String paymentGateway(@RequestParam Long bookingId, HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) {
                return "redirect:/user/login";
            }

            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                model.addAttribute("error", "Booking not found");
                return "user/payment-gateway";
            }

            Booking booking = bookingOpt.get();
            if (!booking.getUser().getId().equals(user.getId())) {
                model.addAttribute("error", "Unauthorized access to booking");
                return "user/payment-gateway";
            }

            // Handle completed bookings that may not have parking slot
            if (booking.getParkingSlot() == null) {
                model.addAttribute("error", "Cannot process payment for completed booking");
                return "user/payment-gateway";
            }

            // Find the most recent completed VehicleLog for this vehicle and parking lot
            Optional<VehicleLog> vehicleLogOpt = vehicleLogRepository.findTopByVehicleAndExitTimeIsNotNullOrderByEntryTimeDesc(booking.getVehicle());
            if (!vehicleLogOpt.isPresent() || !vehicleLogOpt.get().getParkingLot().getId().equals(booking.getParkingSlot().getParkingLot().getId())) {
                model.addAttribute("error", "Vehicle has not exited yet or no valid log found for this parking lot");
                return "user/payment-gateway";
            }

            VehicleLog vehicleLog = vehicleLogOpt.get();

            Optional<ParkingLot> parkingLotOpt = parkingLotRepository.findById(booking.getParkingSlot().getParkingLot().getId());
            if (!parkingLotOpt.isPresent()) {
                model.addAttribute("error", "Parking lot not found");
                return "user/payment-gateway";
            }

            ParkingLot parkingLot = parkingLotOpt.get();

            long hours = ChronoUnit.HOURS.between(vehicleLog.getEntryTime(), vehicleLog.getExitTime());
            long minutes = ChronoUnit.MINUTES.between(vehicleLog.getEntryTime(), vehicleLog.getExitTime()) % 60;

            // More accurate calculation: if there are any minutes beyond the hour, charge for the next hour
            if (minutes > 0) {
                hours += 1;
            }

            // Minimum 1 hour charge
            if (hours == 0) hours = 1;
            BigDecimal hoursBigDecimal = new BigDecimal(hours);
            BigDecimal amount = hoursBigDecimal.multiply(parkingLot.getPriceHr());

            model.addAttribute("selectedBooking", booking);
            model.addAttribute("vehicleLog", vehicleLog);
            model.addAttribute("hours", hours);
            model.addAttribute("pricePerHour", parkingLot.getPriceHr());
            model.addAttribute("amount", amount);

            return "user/payment-gateway";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load payment page: " + e.getMessage());
            return "user/payment-gateway";
        }
    }

    @PostMapping("/processPayment")
    public String processPayment(@RequestParam Long bookingId,
                                 @RequestParam String paymentMethod,
                                 HttpSession session,
                                 Model model) {
        try {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) {
                return "redirect:/user/login";
            }

            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (!bookingOpt.isPresent()) {
                model.addAttribute("error", "Booking not found");
                return "user/payment-gateway";
            }

            Booking booking = bookingOpt.get();
            if (!booking.getUser().getId().equals(user.getId())) {
                model.addAttribute("error", "Unauthorized access to booking");
                return "user/payment-gateway";
            }

            if (!booking.getPaymentStatus().name().equals("Pending")) {
                model.addAttribute("error", "Payment already processed or invalid status");
                return "user/payment-gateway";
            }

            // Handle completed bookings that may not have parking slot
            if (booking.getParkingSlot() == null) {
                model.addAttribute("error", "Cannot process payment for completed booking");
                return "user/payment-gateway";
            }

            // Find the most recent completed VehicleLog for this vehicle and parking lot
            Optional<VehicleLog> vehicleLogOpt = vehicleLogRepository.findTopByVehicleAndExitTimeIsNotNullOrderByEntryTimeDesc(booking.getVehicle());
            if (!vehicleLogOpt.isPresent() || !vehicleLogOpt.get().getParkingLot().getId().equals(booking.getParkingSlot().getParkingLot().getId())) {
                model.addAttribute("error", "Vehicle has not exited yet or no valid log found for this parking lot");
                return "user/payment-gateway";
            }

            VehicleLog vehicleLog = vehicleLogOpt.get();

            Optional<ParkingLot> parkingLotOpt = parkingLotRepository.findById(booking.getParkingSlot().getParkingLot().getId());
            if (!parkingLotOpt.isPresent()) {
                model.addAttribute("error", "Parking lot not found");
                return "user/payment-gateway";
            }

            ParkingLot parkingLot = parkingLotOpt.get();

            long hours = ChronoUnit.HOURS.between(vehicleLog.getEntryTime(), vehicleLog.getExitTime());
            long minutes = ChronoUnit.MINUTES.between(vehicleLog.getEntryTime(), vehicleLog.getExitTime()) % 60;

            // More accurate calculation: if there are any minutes beyond the hour, charge for the next hour
            if (minutes > 0) {
                hours += 1;
            }

            // Minimum 1 hour charge
            if (hours == 0) hours = 1;
            BigDecimal hoursBigDecimal = new BigDecimal(hours);
            BigDecimal amount = hoursBigDecimal.multiply(parkingLot.getPriceHr());

            // Process payment using strategy pattern
            if (!paymentProcessingService.isValidPaymentMethod(paymentMethod)) {
                model.addAttribute("error", "Invalid payment method: " + paymentMethod);
                return "user/payment-gateway";
            }
            
            Payment payment = paymentProcessingService.processPayment(booking, amount, paymentMethod);

            // Update booking with end time and payment status
            booking.setEndTime(vehicleLog.getExitTime());
            booking.setPaymentStatus(Booking.PaymentStatus.Completed);

            // Update parking slot status to AVAILABLE
            ParkingSlot slot = booking.getParkingSlot();
            slot.setStatus(ParkingSlot.SlotStatus.AVAILABLE);

            // Save all entities
            paymentRepository.save(payment);
            bookingRepository.save(booking);
            parkingSlotRepository.save(slot);

            // Send email notification
            try {
                sendPaymentConfirmationEmail(user, booking, payment, vehicleLog, parkingLot, hours);
            } catch (Exception emailException) {
                // Log email error but don't fail the payment
                System.err.println("Failed to send email notification: " + emailException.getMessage());
            }

            model.addAttribute("success", "Payment processed successfully for LKR " + amount.toString() + ". A confirmation email has been sent to your registered email address.");
            return "redirect:/user/payments";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to process payment: " + e.getMessage());
            return "user/payment-gateway";
        }
    }

    @GetMapping("/payments")
    public String paymentHistory(HttpSession session, Model model) {
        try {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) {
                return "redirect:/user/login";
            }

            List<Booking> userBookings = bookingRepository.findByUserIdWithParkingDetailsOrderByStartTimeDesc(user.getId());
            List<Payment> userPayments = paymentRepository.findAll().stream()
                    .filter(payment -> userBookings.stream().anyMatch(booking -> booking.getId().equals(payment.getBooking().getId())))
                    .toList();

            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            Double totalSpentToday = paymentRepository.sumAmountByCompletedAndTimestampAfter(startOfDay);
            if (totalSpentToday == null) totalSpentToday = 0.0;

            model.addAttribute("userId", user.getId());
            model.addAttribute("userName", user.getName());
            model.addAttribute("bookings", userBookings);
            model.addAttribute("payments", userPayments);
            model.addAttribute("totalSpentToday", totalSpentToday);

            return "user/payments";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load payment history: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/receipt/download/{paymentId}")
    public ResponseEntity<String> downloadReceipt(@PathVariable Long paymentId, HttpSession session) {
        try {
            User user = (User) session.getAttribute("loggedInUser");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
            }

            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (!paymentOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Payment payment = paymentOpt.get();
            Booking booking = payment.getBooking();

            if (!booking.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            // Generate a simple receipt content (in a real application, you'd generate a PDF)
            StringBuilder receipt = new StringBuilder();
            receipt.append("PARQHUB PARKING RECEIPT\n");
            receipt.append("========================\n\n");
            receipt.append("Receipt #: PQH-").append(paymentId).append("\n");
            receipt.append("Date: ").append(payment.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            receipt.append("Customer: ").append(user.getName()).append("\n");
            receipt.append("Email: ").append(user.getEmail()).append("\n\n");

            receipt.append("VEHICLE INFORMATION\n");
            receipt.append("-------------------\n");
            receipt.append("Vehicle: ").append(booking.getVehicle().getBrand()).append(" ").append(booking.getVehicle().getModel()).append("\n");
            receipt.append("License: ").append(booking.getVehicle().getVehicleNo()).append("\n\n");

            receipt.append("PARKING DETAILS\n");
            receipt.append("---------------\n");
            if (booking.getParkingSlot() != null) {
                receipt.append("Location: ").append(booking.getParkingSlot().getParkingLot().getLocation()).append(", ").append(booking.getParkingSlot().getParkingLot().getCity()).append("\n");
                receipt.append("Slot ID: ").append(booking.getParkingSlot().getId()).append("\n");
            }
            receipt.append("Start Time: ").append(booking.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            if (booking.getEndTime() != null) {
                receipt.append("End Time: ").append(booking.getEndTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
                receipt.append("Duration: ").append(booking.getFormattedDuration()).append("\n");
            }
            receipt.append("\n");

            receipt.append("PAYMENT INFORMATION\n");
            receipt.append("-------------------\n");
            receipt.append("Amount: LKR ").append(payment.getAmount()).append("\n");
            receipt.append("Method: ").append(payment.getMethod()).append("\n");
            receipt.append("Status: ").append(payment.getStatus()).append("\n\n");

            receipt.append("Thank you for using ParQHub!\n");
            receipt.append("Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "parqhub-receipt-" + paymentId + ".txt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(receipt.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate receipt: " + e.getMessage());
        }
    }

    private void sendPaymentConfirmationEmail(User user, Booking booking, Payment payment,
                                              VehicleLog vehicleLog, ParkingLot parkingLot, long hours) {
        try {
            if (mailSender == null) {
                System.out.println("Mail sender not configured. Skipping email notification.");
                return;
            }

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                System.err.println("User email is empty for user: " + user.getName());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail().trim());
            message.setSubject("ParQHub - Payment Confirmation Receipt #PQH-" + payment.getId());

            StringBuilder emailBody = new StringBuilder();
            emailBody.append("Dear ").append(user.getName()).append(",\n\n");
            emailBody.append("Thank you for using ParQHub! Your payment has been successfully processed.\n\n");

            emailBody.append("PAYMENT CONFIRMATION\n");
            emailBody.append("=====================\n\n");

            emailBody.append("Receipt Number: PQH-").append(payment.getId()).append("\n");
            emailBody.append("Payment Date: ").append(payment.getTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            emailBody.append("Payment Status: ").append(payment.getStatus()).append("\n\n");

            emailBody.append("BOOKING DETAILS\n");
            emailBody.append("===============\n\n");
            emailBody.append("Booking ID: ").append(booking.getId()).append("\n");
            emailBody.append("Vehicle: ").append(booking.getVehicle().getBrand()).append(" ").append(booking.getVehicle().getModel()).append("\n");
            emailBody.append("License Plate: ").append(booking.getVehicle().getVehicleNo()).append("\n");

            if (booking.getParkingSlot() != null && booking.getParkingSlot().getParkingLot() != null) {
                emailBody.append("Location: ").append(booking.getParkingSlot().getParkingLot().getLocation()).append(", ").append(booking.getParkingSlot().getParkingLot().getCity()).append("\n");
                emailBody.append("Parking Slot: ").append(booking.getParkingSlot().getId()).append("\n");
            }

            emailBody.append("\nPARKING DURATION\n");
            emailBody.append("================\n\n");
            emailBody.append("Entry Time: ").append(vehicleLog.getEntryTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            emailBody.append("Exit Time: ").append(vehicleLog.getExitTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            emailBody.append("Total Duration: ").append(hours).append(" hour(s)\n");

            emailBody.append("\nPAYMENT BREAKDOWN\n");
            emailBody.append("=================\n\n");
            emailBody.append("Hourly Rate: LKR ").append(parkingLot.getPriceHr()).append("\n");
            emailBody.append("Duration: ").append(hours).append(" hour(s)\n");
            emailBody.append("Total Amount: LKR ").append(payment.getAmount()).append("\n");
            emailBody.append("Payment Method: ").append(payment.getMethod()).append("\n\n");

            emailBody.append("Thank you for choosing ParQHub for your parking needs!\n\n");
            emailBody.append("For any queries, please contact our support team.\n\n");
            emailBody.append("Best regards,\n");
            emailBody.append("ParQHub Team\n");
            emailBody.append("\n---\n");
            emailBody.append("This is an automated message. Please do not reply to this email.");

            message.setText(emailBody.toString());
            message.setFrom(fromEmail);

            mailSender.send(message);
            System.out.println("✓ Payment confirmation email sent successfully to: " + user.getEmail());
            System.out.println("✓ Email content preview: Receipt #PQH-" + payment.getId() + " for LKR " + payment.getAmount());

        } catch (Exception e) {
            System.err.println("✗ Error sending email to " + user.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
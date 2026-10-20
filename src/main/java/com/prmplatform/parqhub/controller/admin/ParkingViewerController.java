package com.prmplatform.parqhub.controller.admin;

import com.prmplatform.parqhub.model.Admin;
import com.prmplatform.parqhub.model.ParkingLot;
import com.prmplatform.parqhub.model.ParkingSlot;
import com.prmplatform.parqhub.service.ParkingViewerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class ParkingViewerController {

    private final ParkingViewerService parkingViewerService;

    public ParkingViewerController(ParkingViewerService parkingViewerService) {
        this.parkingViewerService = parkingViewerService;
    }

    @GetMapping("/parking-viewer")
    public String parkingViewer(@RequestParam(required = false) String city,
                                @RequestParam(required = false) String location,
                                HttpSession session,
                                Model model) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("adminName", admin.getName());
        model.addAttribute("adminRole", admin.getRole());

        List<String> cities = parkingViewerService.getAllCities();
        model.addAttribute("cities", cities);

        if (city != null && location != null) {
            ParkingLot lot = parkingViewerService.getParkingLotByCityAndLocation(city, location)
                    .orElse(null);
            if (lot != null) {
                ParkingViewerService.ParkingLotView lotView = parkingViewerService.getParkingLotView(lot.getId());
                if (lotView != null) {
                    model.addAttribute("selectedLot", lotView.getParkingLot());
                    model.addAttribute("parkingSlots", lotView.getParkingSlots());
                    model.addAttribute("totalSlots", lotView.getParkingLot().getTotalSlots());

                    ParkingViewerService.ParkingSlotStatistics stats = lotView.getStatistics();
                    model.addAttribute("availableCount", stats.getAvailableCount());
                    model.addAttribute("bookedCount", stats.getBookedCount());
                    model.addAttribute("occupiedCount", stats.getOccupiedCount());
                    model.addAttribute("totalCount", stats.getTotalCount());
                    model.addAttribute("statistics", stats);
                }
            }
        }

        return "admin/parkingViewer";
    }

    @GetMapping("/api/cities")
    @ResponseBody
    public ResponseEntity<List<String>> getAllCities() {
        return ResponseEntity.ok(parkingViewerService.getAllCities());
    }

    @GetMapping("/api/locations")
    @ResponseBody
    public ResponseEntity<List<String>> getLocationsByCity(@RequestParam String city) {
        return ResponseEntity.ok(parkingViewerService.getLocationsByCity(city));
    }

    @GetMapping("/api/parking-slots/{lotId}")
    @ResponseBody
    public ResponseEntity<List<ParkingSlot>> getParkingSlots(@PathVariable Long lotId) {
        if (!parkingViewerService.parkingLotExists(lotId)) {
            return ResponseEntity.notFound().build();
        }

        List<ParkingSlot> slots = parkingViewerService.getParkingSlotsByLotId(lotId);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/api/parking-statistics/{lotId}")
    @ResponseBody
    public ResponseEntity<ParkingViewerService.ParkingSlotStatistics> getParkingStatistics(@PathVariable Long lotId) {
        if (!parkingViewerService.parkingLotExists(lotId)) {
            return ResponseEntity.notFound().build();
        }

        ParkingViewerService.ParkingSlotStatistics stats = parkingViewerService.getParkingSlotStatistics(lotId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/parking-lot/{lotId}")
    @ResponseBody
    public ResponseEntity<ParkingLot> getParkingLot(@PathVariable Long lotId) {
        return parkingViewerService.getParkingLotById(lotId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/parking-lot")
    @ResponseBody
    public ResponseEntity<ParkingLot> getParkingLotByCityAndLocation(@RequestParam String city,
                                                                     @RequestParam String location) {
        return parkingViewerService.getParkingLotByCityAndLocation(city, location)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
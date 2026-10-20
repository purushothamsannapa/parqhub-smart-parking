package com.prmplatform.parqhub.controller.user;

import com.prmplatform.parqhub.model.ParkingLot;
import com.prmplatform.parqhub.model.ParkingSlot;
import com.prmplatform.parqhub.model.User;
import com.prmplatform.parqhub.model.Vehicle;
import com.prmplatform.parqhub.repository.ParkingLotRepository;
import com.prmplatform.parqhub.repository.VehicleRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class FindParkingController {

    private static final Logger logger = LoggerFactory.getLogger(FindParkingController.class);

    private final ParkingLotRepository parkingLotRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public FindParkingController(ParkingLotRepository parkingLotRepository, VehicleRepository vehicleRepository) {
        this.parkingLotRepository = parkingLotRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/findparking")
    public String showFindParkingForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<Vehicle> userVehicles = vehicleRepository.findByUserId(user.getId());
        List<String> distinctCities = parkingLotRepository.findDistinctCities();
        List<String> distinctLocations = model.containsAttribute("city") && model.getAttribute("city") != null
                ? parkingLotRepository.findDistinctLocationsByCity((String) model.getAttribute("city"))
                : List.of();

        List<ParkingLot> parkingLots = parkingLotRepository.findAllWithSlots();
        Map<Long, Long> availableSlotsCount = parkingLots.stream()
                .collect(Collectors.toMap(
                        ParkingLot::getId,
                        lot -> {
                            long count = lot.getParkingSlots().stream()
                                    .filter(slot -> slot.getStatus() == ParkingSlot.SlotStatus.AVAILABLE)
                                    .count();
                            logger.debug("Parking Lot ID {}: {} available slots", lot.getId(), count);
                            return count;
                        }
                ));

        model.addAttribute("userName", user.getName());
        model.addAttribute("userVehicles", userVehicles);
        model.addAttribute("parkingLots", parkingLots);
        model.addAttribute("availableSlotsCount", availableSlotsCount);
        model.addAttribute("distinctCities", distinctCities);
        model.addAttribute("distinctLocations", distinctLocations);
        model.addAttribute("searchPerformed", false);

        return "user/findparking";
    }

    @PostMapping("/findparking")
    public String searchParkingLots(@RequestParam(required = false) String city,
                                    @RequestParam(required = false) String location,
                                    @RequestParam(required = false) BigDecimal maxPrice,
                                    @RequestParam(required = false, defaultValue = "false") boolean availableOnly,
                                    HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/user/login";
        }

        List<Vehicle> userVehicles = vehicleRepository.findByUserId(user.getId());
        List<String> distinctCities = parkingLotRepository.findDistinctCities();
        List<String> distinctLocations = city != null && !city.trim().isEmpty()
                ? parkingLotRepository.findDistinctLocationsByCity(city.trim())
                : List.of();

        List<ParkingLot> parkingLots;
        if (city == null && location == null && maxPrice == null && !availableOnly) {
            parkingLots = parkingLotRepository.findAllWithSlots();
        } else {
            city = city != null ? city.trim() : "";
            location = location != null ? location.trim() : "";
            parkingLots = parkingLotRepository.findByFilters(
                    city.isEmpty() ? null : city,
                    location.isEmpty() ? null : location,
                    maxPrice,
                    availableOnly
            );
        }

        Map<Long, Long> availableSlotsCount = parkingLots.stream()
                .collect(Collectors.toMap(
                        ParkingLot::getId,
                        lot -> {
                            long count = lot.getParkingSlots().stream()
                                    .filter(slot -> slot.getStatus() == ParkingSlot.SlotStatus.AVAILABLE)
                                    .count();
                            logger.debug("Parking Lot ID {}: {} available slots", lot.getId(), count);
                            return count;
                        }
                ));

        model.addAttribute("userName", user.getName());
        model.addAttribute("userVehicles", userVehicles);
        model.addAttribute("parkingLots", parkingLots);
        model.addAttribute("availableSlotsCount", availableSlotsCount);
        model.addAttribute("distinctCities", distinctCities);
        model.addAttribute("distinctLocations", distinctLocations);
        model.addAttribute("searchPerformed", true);
        model.addAttribute("city", city);
        model.addAttribute("location", location);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("availableOnly", availableOnly);

        return "user/findparking";
    }

    @GetMapping("/locations")
    @ResponseBody
    public List<String> getLocationsByCity(@RequestParam String city) {
        return parkingLotRepository.findDistinctLocationsByCity(city);
    }
}
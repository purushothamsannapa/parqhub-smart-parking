package com.prmplatform.parqhub.controller;

import com.prmplatform.parqhub.model.User;
import com.prmplatform.parqhub.model.Vehicle;
import com.prmplatform.parqhub.repository.UserRepository;
import com.prmplatform.parqhub.repository.VehicleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Controller
public class RegisterController {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public RegisterController(UserRepository userRepository, VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userForm") UserForm userForm, Model model) {
        List<String> errors = new ArrayList<>();
        if (userForm.getName() == null || userForm.getName().trim().isEmpty()) {
            errors.add("Name is required");
        }
        if (userForm.getEmail() == null || userForm.getEmail().trim().isEmpty()) {
            errors.add("Email is required");
        } else if (!isValidEmail(userForm.getEmail())) {
            errors.add("Invalid email format");
        }
        if (userForm.getPassword() == null || userForm.getPassword().trim().isEmpty()) {
            errors.add("Password is required");
        } else if (userForm.getPassword().length() < 6) {
            errors.add("Password must be at least 6 characters");
        }
        if (userForm.getVehicles() == null || userForm.getVehicles().isEmpty()) {
            errors.add("At least one vehicle is required");
        } else {
            for (int i = 0; i < userForm.getVehicles().size(); i++) {
                VehicleForm vehicle = userForm.getVehicles().get(i);
                if (vehicle.getVehicleNo() == null || vehicle.getVehicleNo().trim().isEmpty()) {
                    errors.add("Vehicle number is required for vehicle " + (i + 1));
                }
                if (vehicle.getVehicleType() == null || vehicle.getVehicleType().trim().isEmpty()) {
                    errors.add("Vehicle type is required for vehicle " + (i + 1));
                } else if (!isValidVehicleType(vehicle.getVehicleType())) {
                    errors.add("Invalid vehicle type for vehicle " + (i + 1));
                }
            }
        }
        if (userRepository.findByEmail(userForm.getEmail()).isPresent()) {
            errors.add("Email is already registered");
        }
        if (!errors.isEmpty()) {
            model.addAttribute("error", String.join("; ", errors));
            return "register";
        }

        try {
            User user = new User();
            user.setName(userForm.getName().trim());
            user.setEmail(userForm.getEmail().trim());
            user.setPassword(userForm.getPassword()); // Plain-text password
            user.setContactNo(userForm.getContactNo() != null ? userForm.getContactNo().trim() : null);
            user.setVehicles(new ArrayList<>());
            user = userRepository.save(user);

            for (VehicleForm vehicleForm : userForm.getVehicles()) {
                Vehicle vehicle = new Vehicle();
                vehicle.setUser(user);
                vehicle.setVehicleNo(vehicleForm.getVehicleNo().trim());
                vehicle.setVehicleType(Vehicle.VehicleType.valueOf(vehicleForm.getVehicleType()));
                vehicle.setBrand(vehicleForm.getBrand() != null ? vehicleForm.getBrand().trim() : null);
                vehicle.setModel(vehicleForm.getModel() != null ? vehicleForm.getModel().trim() : null);
                vehicle.setColor(vehicleForm.getColor() != null ? vehicleForm.getColor().trim() : null);
                vehicleRepository.save(vehicle);
            }

            model.addAttribute("success", "Registration successful! Please log in.");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidVehicleType(String vehicleType) {
        try {
            Vehicle.VehicleType.valueOf(vehicleType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static class UserForm {
        private String name;
        private String email;
        private String password;
        private String contactNo;
        private List<VehicleForm> vehicles = new ArrayList<>();

        public UserForm() {
            vehicles.add(new VehicleForm());
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getContactNo() { return contactNo; }
        public void setContactNo(String contactNo) { this.contactNo = contactNo; }
        public List<VehicleForm> getVehicles() { return vehicles; }
        public void setVehicles(List<VehicleForm> vehicles) { this.vehicles = vehicles; }
    }

    public static class VehicleForm {
        private String vehicleNo;
        private String vehicleType;
        private String brand;
        private String model;
        private String color;

        public String getVehicleNo() { return vehicleNo; }
        public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }
        public String getVehicleType() { return vehicleType; }
        public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}
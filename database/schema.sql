-- ========================================================
-- ParQHub Database Schema
-- Database: parqhub_db
-- ========================================================

CREATE DATABASE IF NOT EXISTS parqhub_db;
USE parqhub_db;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Admins Table
DROP TABLE IF EXISTS admins;
CREATE TABLE admins (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('SUPER_ADMIN', 'OPERATIONS_MANAGER', 'FINANCE_OFFICER', 'SECURITY_SUPERVISOR', 'IT_SUPPORT', 'CUSTOMER_SERVICE_OFFICER') NOT NULL DEFAULT 'SUPER_ADMIN'
);

-- 2. Users (Customer Drivers) Table
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    contact_no VARCHAR(20)
);

-- 3. Vehicle Table
DROP TABLE IF EXISTS vehicle;
CREATE TABLE vehicle (
    vehicle_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    vehicle_no VARCHAR(20) UNIQUE NOT NULL,
    vehicle_type ENUM('Car', 'Bike', 'Van', 'Truck') NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(50),
    color VARCHAR(30),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 4. ParkingLot Table
DROP TABLE IF EXISTS parkinglot;
CREATE TABLE parkinglot (
    lot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    city VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    total_slots INT NOT NULL,
    price_hr DECIMAL(10,2) NOT NULL DEFAULT 0.00
);

-- 5. ParkingSlot Table
DROP TABLE IF EXISTS parkingslot;
CREATE TABLE parkingslot (
    slot_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    lot_id BIGINT NOT NULL,
    status ENUM('AVAILABLE', 'BOOKED', 'OCCUPIED') DEFAULT 'AVAILABLE',
    FOREIGN KEY (lot_id) REFERENCES parkinglot(lot_id) ON DELETE CASCADE
);

-- 6. Booking Table
DROP TABLE IF EXISTS booking;
CREATE TABLE booking (
    booking_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    payment_status ENUM('Pending', 'Completed', 'Failed') DEFAULT 'Pending',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES parkingslot(slot_id) ON DELETE CASCADE
);

-- 7. Payment Table
DROP TABLE IF EXISTS payment;
CREATE TABLE payment (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method ENUM('Card', 'Cash', 'Arrival') DEFAULT 'Arrival',
    status ENUM('Pending', 'Completed', 'Failed') DEFAULT 'Pending',
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
);

-- 8. VehicleLog Table (Entry / Exit logs)
DROP TABLE IF EXISTS vehicle_log;
CREATE TABLE vehicle_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    lot_id BIGINT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME,
    FOREIGN KEY (vehicle_id) REFERENCES vehicle(vehicle_id) ON DELETE CASCADE,
    FOREIGN KEY (lot_id) REFERENCES parkinglot(lot_id) ON DELETE CASCADE
);

-- 9. Report Table
DROP TABLE IF EXISTS report;
CREATE TABLE report (
    report_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type ENUM('Financial', 'Occupancy', 'Performance') DEFAULT 'Performance',
    generated_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    admin_id BIGINT NOT NULL,
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE CASCADE
);

-- 10. Notification Table
DROP TABLE IF EXISTS notification;
CREATE TABLE notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type ENUM('FULL_SLOT', 'OVERSTAY', 'SECURITY_INCIDENT', 'NONE') DEFAULT 'NONE',
    description TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT,
    admin_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES admins(admin_id) ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================
-- Stored Procedures for Dynamic Parking Slot Management
-- ========================================================

DELIMITER $$

-- Procedure to create parking slots for a parking lot
DROP PROCEDURE IF EXISTS create_parking_slots_for_lot$$
CREATE PROCEDURE create_parking_slots_for_lot(IN p_lot_id BIGINT, IN p_total_slots INT)
BEGIN
    DECLARE v_counter INT DEFAULT 1;
    DELETE FROM parkingslot WHERE lot_id = p_lot_id;
    WHILE v_counter <= p_total_slots DO
        INSERT INTO parkingslot (lot_id, status) VALUES (p_lot_id, 'AVAILABLE');
        SET v_counter = v_counter + 1;
    END WHILE;
END$$

-- Procedure to update parking slots when capacity changes
DROP PROCEDURE IF EXISTS update_parking_slots_for_lot$$
CREATE PROCEDURE update_parking_slots_for_lot(IN p_lot_id BIGINT, IN p_old_total_slots INT, IN p_new_total_slots INT)
BEGIN
    DECLARE v_slots_to_add INT;
    DECLARE v_slots_to_remove INT;
    DECLARE v_counter INT DEFAULT 1;

    IF p_new_total_slots > p_old_total_slots THEN
        SET v_slots_to_add = p_new_total_slots - p_old_total_slots;
        WHILE v_counter <= v_slots_to_add DO
            INSERT INTO parkingslot (lot_id, status) VALUES (p_lot_id, 'AVAILABLE');
            SET v_counter = v_counter + 1;
        END WHILE;
    ELSEIF p_new_total_slots < p_old_total_slots THEN
        SET v_slots_to_remove = p_old_total_slots - p_new_total_slots;
        DELETE FROM parkingslot 
        WHERE lot_id = p_lot_id 
        ORDER BY slot_id DESC 
        LIMIT v_slots_to_remove;
    END IF;
END$$

-- Procedure to delete all slots when a parking lot is deleted
DROP PROCEDURE IF EXISTS delete_parking_slots_for_lot$$
CREATE PROCEDURE delete_parking_slots_for_lot(IN p_lot_id BIGINT)
BEGIN
    DELETE FROM parkingslot WHERE lot_id = p_lot_id;
END$$

DELIMITER ;

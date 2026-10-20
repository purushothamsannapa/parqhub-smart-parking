-- ========================================================
-- ParQHub Initial Seed Data
-- ========================================================

USE parqhub_db;

-- 1. Insert Parking Lots (14 locations across 3 major cities)
INSERT INTO parkinglot (city, location, total_slots, price_hr) VALUES
('Colombo', 'Pettah', 50, 250.00),
('Colombo', 'Galle Face', 35, 200.00),
('Colombo', 'Maligakanda', 24, 140.00),
('Colombo', 'Kollupitiya', 30, 180.00),
('Colombo', 'SSC', 22, 100.00),
('Galle', 'Galle Dutch Fort', 50, 250.00),
('Galle', 'Galle ICS', 35, 200.00),
('Galle', 'Galle Beach', 24, 140.00),
('Galle', 'Flag Rock Bastion', 30, 180.00),
('Galle', 'Galle Museum', 22, 100.00),
('Kandy', 'Dalada Maligawa', 50, 250.00),
('Kandy', 'Raja Veediya', 35, 200.00),
('Kandy', 'Old Royal Palace', 24, 140.00),
('Kandy', 'Lakeside', 30, 180.00);

-- 2. Populate Slots for all Lots
DELIMITER $$
DROP PROCEDURE IF EXISTS populate_all_parking_slots$$
CREATE PROCEDURE populate_all_parking_slots()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_lot_id BIGINT;
    DECLARE v_total_slots INT;
    DECLARE v_counter INT;

    DECLARE lot_cursor CURSOR FOR
        SELECT lot_id, total_slots FROM parkinglot;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN lot_cursor;

    read_loop: LOOP
        FETCH lot_cursor INTO v_lot_id, v_total_slots;
        IF done THEN
            LEAVE read_loop;
        END IF;

        SET v_counter = 1;
        WHILE v_counter <= v_total_slots DO
            INSERT INTO parkingslot (lot_id, status) VALUES (v_lot_id, 'AVAILABLE');
            SET v_counter = v_counter + 1;
        END WHILE;
    END LOOP;

    CLOSE lot_cursor;
END$$
DELIMITER ;

CALL populate_all_parking_slots();
DROP PROCEDURE populate_all_parking_slots;

-- 3. Default Admin Accounts (All RBAC Roles)
INSERT INTO admins (name, email, password, role) VALUES 
('Super Admin', 'admin@parqhub.com', 'admin123', 'SUPER_ADMIN'),
('Ops Manager', 'ops@parqhub.com', 'admin123', 'OPERATIONS_MANAGER'),
('Finance Officer', 'finance@parqhub.com', 'admin123', 'FINANCE_OFFICER'),
('Security Officer', 'security@parqhub.com', 'admin123', 'SECURITY_SUPERVISOR'),
('IT Support', 'itsupport@parqhub.com', 'admin123', 'IT_SUPPORT'),
('Customer Support', 'customerservice@parqhub.com', 'admin123', 'CUSTOMER_SERVICE_OFFICER');

-- 4. Demo Customer Account
INSERT INTO users (name, email, password, contact_no) VALUES 
('Demo User', 'user@parqhub.com', 'user123', '0771234567');

-- 5. Demo Vehicle for Demo User
INSERT INTO vehicle (user_id, vehicle_no, vehicle_type, brand, model, color) VALUES
(1, 'WP-CAA-1234', 'Car', 'Toyota', 'Prius', 'White');

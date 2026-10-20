# 🚗 ParQHub - Smart Parking Management System

[![Java](https://img.shields.io/badge/Java-21%20%7C%2025-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> **ParQHub** is an enterprise-grade automated vehicle parking reservation and management platform designed to streamline urban parking across major cities.

---

## 📑 Table of Contents

1. [System Overview](#-system-overview)
2. [Clean Project Directory Structure](#-clean-project-directory-structure)
3. [Quick Start & Running Instructions](#-quick-start--running-instructions)
4. [Running in Visual Studio Code](#-running-in-visual-studio-code)
5. [Database Architecture & Setup](#-database-architecture--setup)
6. [Pre-Seeded Credentials & Roles](#-pre-seeded-credentials--roles)
7. [API Endpoints & Route Map](#-api-endpoints--route-map)
8. [Design Patterns Implemented](#-design-patterns-implemented)
9. [Cleanup & Modernization Changelog](#-cleanup--modernization-changelog)

---

## 🌟 System Overview

ParQHub manages parking lots, live slot inventory, vehicle registration, driver reservations, automated entry/exit tracking, and multi-method payments. It features two dedicated interfaces:

- **Customer Portal**: Dark blue glassmorphism interface allowing drivers to search for available parking by city, book spots in advance, log arrivals/departures, pay fees, and download receipts.
- **Admin Portal**: Crimson security-oriented operations dashboard featuring granular Role-Based Access Control (RBAC) powered by the Strategy Pattern for Operations Managers, Finance Officers, Security Supervisors, IT Support, Customer Service, and Super Admins.

### Tech Stack
- **Backend**: Spring Boot 3.3.4, Java 21 / 25, Spring Data JPA, Hibernate 6.5.3
- **Database**: MySQL 8.0 (`parqhub_db`)
- **Frontend**: Thymeleaf, Tailwind CSS 3.x, Font Awesome 6.4.0, Particles.js, AOS animations
- **Build Tool**: Apache Maven 3.9.x via included Maven Wrapper (`mvnw.cmd`)

---

## 📂 Clean Project Directory Structure

The project has been cleaned of redundant directories (`parqHub-master` duplicate copy, `bin/` dump, obsolete `.github/modernize` upgrade logs, and conflicting root SQL files):

```
parqHub-master/
├── .mvn/                                # Maven wrapper configuration
│   └── wrapper/
│       ├── maven-wrapper.jar
│       └── maven-wrapper.properties
├── .vscode/                             # VS Code workspace integration
│   ├── extensions.json                  # Recommended extensions
│   ├── launch.json                      # One-click F5 Run & Debug configuration
│   ├── settings.json                    # Workspace Java settings
│   └── tasks.json                       # Terminal build tasks
├── database/                            # Unified database scripts
│   ├── schema.sql                       # Complete DDL tables & stored procedures
│   └── seed.sql                         # 14 parking lots, 461 slots, 6 admin roles & demo driver
├── src/
│   ├── main/
│   │   ├── java/com/prmplatform/parqhub/
│   │   │   ├── ParqHubApplication.java  # Main application entry point
│   │   │   ├── controller/              # MVC Web & REST Controllers
│   │   │   │   ├── CustomErrorController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── admin/               # Administrative controllers
│   │   │   │   │   ├── AdminController.java
│   │   │   │   │   ├── AdminManageSlotController.java
│   │   │   │   │   ├── ManageTablesController.java
│   │   │   │   │   ├── ParkingViewerController.java
│   │   │   │   │   └── ReportController.java
│   │   │   │   └── user/                # Driver portal controllers
│   │   │   │       ├── FindParkingController.java
│   │   │   │       ├── PaymentController.java
│   │   │   │       ├── UserController.java
│   │   │   │       └── VehicleLogController.java
│   │   │   ├── model/                   # JPA Entity Models & DTOs
│   │   │   │   ├── Admin.java
│   │   │   │   ├── Booking.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── ParkingLot.java
│   │   │   │   ├── ParkingSlot.java
│   │   │   │   ├── ParkingSlotDTO.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Report.java
│   │   │   │   ├── User.java
│   │   │   │   ├── Vehicle.java
│   │   │   │   ├── VehicleDTO.java
│   │   │   │   └── VehicleLog.java
│   │   │   ├── repository/              # Spring Data JPA Repositories
│   │   │   │   ├── AdminRepository.java
│   │   │   │   ├── BookingRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── ParkingLotRepository.java
│   │   │   │   ├── ParkingSlotRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── ReportRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── VehicleLogRepository.java
│   │   │   │   └── VehicleRepository.java
│   │   │   ├── service/                 # Service Layer
│   │   │   │   ├── AdminAuthenticationService.java
│   │   │   │   ├── ParkingLotService.java
│   │   │   │   ├── ParkingViewerService.java
│   │   │   │   └── PaymentProcessingService.java
│   │   │   └── strategy/                # Strategy Pattern Implementations
│   │   │       ├── adminauth/           # RBAC Strategies for 6 Admin Roles
│   │   │       └── payment/             # Card, Cash & Arrival Payment Strategies
│   │   └── resources/
│   │       ├── application.properties   # Database and Hibernate configuration
│   │       ├── static/images/           # UI branding assets & avatars
│   │       └── templates/               # Thymeleaf HTML views (admin, user, public)
│   └── test/java/com/prmplatform/parqhub/
│       └── ParqHubApplicationTests.java # Context loading integration tests
├── .gitattributes
├── .gitignore                           # Comprehensive ignore rules
├── mvnw & mvnw.cmd                      # Cross-platform Maven wrappers
├── pom.xml                              # Clean build descriptor
└── README.md                            # Primary project documentation
```

---

## 🚀 Quick Start & Running Instructions

### 1. Prerequisites
- **Java**: JDK 21 or JDK 25 installed.
- **MySQL**: MySQL 8.0 running locally on port `3306`.
  ```powershell
  Get-Service MySQL80
  ```

### 2. Run the Application
Open your terminal in the project directory and execute:
```powershell
# Run the application
.\mvnw.cmd spring-boot:run
```

To run automated tests:
```powershell
.\mvnw.cmd test
```

### 3. Open in Browser
- **Landing Page**: [http://localhost:8080/](http://localhost:8080/)
- **User Portal**: [http://localhost:8080/user/login](http://localhost:8080/user/login)
- **Admin Portal**: [http://localhost:8080/admin/login](http://localhost:8080/admin/login)
- **User Registration**: [http://localhost:8080/register](http://localhost:8080/register)

---

## 💻 Running in Visual Studio Code

The project is pre-configured with `.vscode/launch.json` and `.vscode/tasks.json`:

### Option A: Via Integrated Terminal
1. Open the project in VS Code (`File` ➔ `Open Folder...`).
2. Press <kbd>Ctrl</kbd> + <kbd>`</kbd> to open the integrated terminal.
3. Run:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

### Option B: Via One-Click "Run and Debug" (F5)
1. Install the **Extension Pack for Java** in VS Code.
2. Press <kbd>F5</kbd> on your keyboard (or click **Run** above `main` in `ParqHubApplication.java`).

---

## 🗄️ Database Architecture & Setup

### Database Scripts (in `database/`)
- **`database/schema.sql`**: Creates `parqhub_db`, all 10 tables, foreign key constraints, and dynamic slot management procedures.
- **`database/seed.sql`**: Seeds 14 parking lots, 461 parking slots, 6 role-based admin accounts, and a demo driver with a vehicle.

To reset or initialize the database manually:
```powershell
Get-Content database/schema.sql | mysql -u root -p parqhub_db
Get-Content database/seed.sql | mysql -u root -p parqhub_db
```

### Configuration (`src/main/resources/application.properties`)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/parqhub_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

## 🔑 Pre-Seeded Credentials & Roles

### 🛡️ Administrative Accounts
| Role | Email | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Super Admin** | `admin@parqhub.com` | `admin123` | Full access across all modules |
| **Operations Manager** | `ops@parqhub.com` | `admin123` | Slot occupancy, live viewer, capacity management |
| **Finance Officer** | `finance@parqhub.com` | `admin123` | Financial reports, payment reconciliation, revenue logs |
| **Security Supervisor** | `security@parqhub.com` | `admin123` | Active vehicle logs, security incidents |
| **IT Support** | `itsupport@parqhub.com` | `admin123` | System health, uptime metrics, error logs |
| **Customer Service** | `customerservice@parqhub.com` | `admin123` | Driver lookup, booking inquiries |

### 👤 Demo Driver Account
| Role | Email | Password | Pre-Registered Vehicle | Contact |
| :--- | :--- | :--- | :--- | :--- |
| **Customer** | `user@parqhub.com` | `user123` | `WP-CAA-1234` (Toyota Prius, White) | `0771234567` |

---

## 🌐 API Endpoints & Route Map

### Public Endpoints
- `GET /`: Landing page
- `GET /user/login` (or `/user-login`): Customer login form
- `GET /admin/login` (or `/admin-login`): Admin login form
- `GET /register`: Customer registration wizard
- `POST /register`: Process registration

### Customer Portal (`/user/...`)
- `POST /user/login`: Authenticate customer
- `GET /user/dashboard`: Customer dashboard
- `GET /user/findparking`: Live parking space search & booking
- `POST /user/book-slot`: Reserve a parking slot
- `GET /user/bookings`: View reservations
- `GET /user/vehicles`: View registered vehicles
- `POST /user/vehicles/add`: Add new vehicle
- `POST /user/log-entry`: Check in vehicle to parking lot
- `POST /user/log-exit`: Check out vehicle and compute charges
- `GET /user/payment-gateway`: Payment review and strategy selection
- `POST /user/processPayment`: Complete payment (Card, Cash, Arrival)
- `GET /user/payments`: Transaction history
- `GET /user/receipt/download/{paymentId}`: Download plain-text parking receipt
- `GET /user/notifications`: View system alerts
- `GET /user/settings`: Account preferences
- `GET /user/logout`: Sign out

### Administrative Portal (`/admin/...`)
- `POST /admin/login`: Authenticate admin
- `GET /admin/dashboard`: Role-customized operations dashboard
- `GET /admin/api/dashboard/stats`: Real-time JSON metrics
- `GET /admin/api/dashboard/health`: Real-time health statistics
- `GET /admin/parking-viewer`: Graphical parking lot floor plan
- `GET /admin/manage-parking-slot`: Slot inspection & customer notifications
- `GET /admin/tables`: Universal entity CRUD manager
- `GET /admin/reports`: Business reports dashboard
- `GET /admin/api/reports/financial/export-csv`: CSV financial export
- `GET /admin/api/reports/occupancy/export-csv`: CSV occupancy export
- `GET /admin/logout`: Sign out

---

## 🎨 Design Patterns Implemented

1. **Strategy Pattern (Payment Processing)**:
   - `PaymentStrategy` interface implemented by `CardPaymentStrategy`, `CashPaymentStrategy`, and `ArrivalPaymentStrategy`. `PaymentProcessingService` selects the strategy dynamically at runtime.
2. **Strategy Pattern (Admin RBAC)**:
   - `AdminAuthenticationStrategy` interface implemented for each of the 6 roles (`SuperAdmin`, `OperationsManager`, `FinanceOfficer`, `SecuritySupervisor`, `ITSupport`, `CustomerService`), delegating authorization dynamically.
3. **Model-View-Controller (MVC)**:
   - Clear separation between REST/MVC controllers, JPA domain models, and Thymeleaf views.
4. **Repository Pattern**:
   - Spring Data JPA repositories abstract database access with clean type safety.
5. **Service Layer Pattern**:
   - Encapsulates business logic away from presentation controllers.

---

## 🧹 Cleanup & Modernization Changelog

During the latest restructuring, the following optimizations were applied:

1. **Eliminated Duplicate Nested Directory**:
   - Removed the redundant nested `parqHub-master/` folder that duplicated the entire project.
2. **Removed IDE Build Dumps**:
   - Deleted the obsolete `bin/` directory which contained a complete stale dump of the repository.
3. **Removed Stale Modernization Artifacts**:
   - Deleted `.github/modernize` containing old failure logs and upgrade scripts.
4. **Consolidated Database Scripts**:
   - Replaced disjoint root SQL files (`crdb.sql`, `db.sql`, `seed.sql`) with a clean `database/` directory containing structured `schema.sql` and `seed.sql`.
5. **Cleaned POM Descriptor**:
   - Cleaned empty comments and streamlined dependencies in `pom.xml`.
6. **Optimized `.gitignore`**:
   - Added comprehensive rules ignoring `bin/`, `target/`, IDE artifacts, and temporary scripts.
7. **Verified Test & Build Integrity**:
   - Confirmed 100% test pass rate (`.\mvnw.cmd test`) and clean compilation across all 52 source files.

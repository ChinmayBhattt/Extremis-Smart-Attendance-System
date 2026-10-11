# 🎓 Extremis — Smart Attendance System

<p align="center">
  <img src="https://img.shields.io/badge/Project-Extremis%20Smart%20Attendance-1D61D2?style=for-the-badge&logo=mortarboard" alt="Extremis" />
  <img src="https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Server-Javalin%206.3%20%2F%20Jetty-0B1E36?style=for-the-badge&logo=java" alt="Javalin" />
  <img src="https://img.shields.io/badge/Database-PostgreSQL%20%26%20SQLite-336791?style=for-the-badge&logo=postgresql&logoColor=white" alt="Database" />
  <img src="https://img.shields.io/badge/Deployment-Vercel%20%26%20Standalone%20JAR-000000?style=for-the-badge&logo=vercel&logoColor=white" alt="Vercel" />
</p>

> **Extremis Smart Attendance Platform**  
> 🌐 **Live Web Application:** [extremis-nu.vercel.app](https://extremis-nu.vercel.app)

---

## 📌 Executive Summary

**Extremis** is an enterprise-grade Smart Attendance and Lecture Verification System. It solves proxy attendance, multi-device credential sharing, and manual attendance overhead through:

1. **🔒 Single-Device Session Lock (Students Only):** Each student account is bound to exactly one active device session. Simultaneous logins from other devices or browsers are strictly blocked.
2. **📸 Zero-Footprint Real-Time Lecture Verification:** When a lecture starts (e.g., 09:00 AM), a 5-minute verification window opens (09:05 AM). Students align their face in an oval reticle with campus GPS geofencing. Snapshots are verified **purely in-memory** and **never saved to disk storage**, guaranteeing complete student privacy.
3. **📊 Bulk Student Sheet Onboarding (CSV/Excel):** Admin and Faculty can upload entire batch sheets with 1-click template generation, automated validation of enrollment numbering, and instant account provisioning.
4. **💾 Dual-Dialect Database Engine:** Supports cloud **PostgreSQL** (Supabase, Neon, AWS RDS) for production scale with zero-config local **SQLite** fallback, complete with an in-app interactive **Database Viewer & CSV Exporter**.

---

## 🏛️ System Architecture

```mermaid
graph TD
    subgraph Frontend [Modern Responsive Web & Swing UI]
        A1[Split-Screen Sign In] --> A2{Role Dispatcher}
        A2 -->|Student| B1[Student Desktop Portal]
        A2 -->|Faculty| B2[Faculty Portal]
        A2 -->|Admin| B3[Admin Dashboard]
        B1 --> C1[Live Webcam Oval Scan & GPS Geofence]
        B2 & B3 --> C2[Bulk Sheet CSV Upload & Preview]
        B3 --> C3[Interactive Database Viewer & CSV Export]
    end

    subgraph Backend [Java 21 Core Server]
        D1[Javalin 6.3 REST Engine]
        D2[AuthService & Single-Session Guard]
        D3[AttendanceService & Lecture Verifier]
        D4[DatabaseManager Dual Dialect]
        D1 --> D2
        D1 --> D3
        D2 & D3 --> D4
    end

    subgraph Storage [Persistence Layer]
        E1[(PostgreSQL Cloud / Production)]
        E2[(SQLite Local Embedded)]
        E3[(In-Browser Resilient Fallback)]
        D4 --> E1
        D4 --> E2
        Frontend -.-> E3
    end

    Frontend <==>|HTTP / REST JSON| Backend
```

---

## 🏷️ Departmental Enrollment Standards

The system supports structured departmental enrollment numbering formats:

| Branch / Department | Enrollment Prefix Format | Example Enrollment Numbers |
| :--- | :--- | :--- |
| **Artificial Intelligence & Data Science** | `24EARAD0...` | `24EARAD001`, `24EARAD002`, `24EARAD003` |
| **Computer Science & Engineering** | `24EARCS0...` | `24EARCS001`, `24EARCS002`, `24EARCS003` |
| **Information Technology** | `24EARIT0...` | `24EARIT001`, `24EARIT002`, `24EARIT003` |
| **Electronics & Communication** | `24EAREC0...` | `24EAREC001`, `24EAREC002`, `24EAREC003` |
| **Electrical Engineering** | `24EAREE0...` | `24EAREE001`, `24EAREE002`, `24EAREE003` |
| **Mechanical Engineering** | `24EARME0...` | `24EARME001`, `24EARME002`, `24EARME003` |

---

## 🔑 Pre-Seeded Demonstration Accounts

| Role | Username / Enrollment No. | Password | Name & Department | Access Level |
| :--- | :--- | :--- | :--- | :--- |
| 🎓 **Student** | `24EARCS001` | `student123` | Aarav Sharma (CS, Sec A) | Schedule, Live Verification, Session Info |
| 🎓 **Student** | `24EARAD001` | `student123` | Priya Rathore (AI & DS, Sec A) | Schedule, Live Verification, Session Info |
| 🎓 **Student** | `24EARIT001` | `student123` | Rohan Verma (IT, Sec B) | Schedule, Live Verification, Session Info |
| 👨‍🏫 **Faculty** | `faculty.rajesh` | `faculty123` | Prof. Rajesh Kumar (CS) | Manage Students, Upload Sheet, Timetable |
| 🛡️ **Admin** | `admin` | `admin123` | System Administrator (Extremis) | Full System & Database Controls |

---

## ✨ Key Feature Walkthrough

### 1. Split-Screen Sign In with Capsule Role Switcher
- Left navy hero (`#0B1E36`) showcases official Extremis branding.
- Segmented capsule toggle: `Student` | `Faculty` | `Admin`.
- 1-click demo buttons for rapid evaluation.
- Live single-device policy notice banner.

### 2. Student Desktop Portal (Expansive 2-Column Layout)
- **Top Navigation Bar:** Live real-time campus clock (`10:00:00 AM · Campus Realtime`), initials avatar badge, student department and enrollment ID.
- **Hero Active Class Card:** High-contrast royal blue gradient hero banner highlighting the ongoing class (e.g., *Data Structures & Algorithms in LT-102*), with a pulsed indicator for open 5-minute verification windows.
- **Schedule Column:** Dynamic timeline cards showing today's lecture schedule, faculty name, room number, scheduled scan times, and live status pills (*Verified (Present)* vs. *Verify Attendance*).
- **Security & Policy Sidebar:**
  - Today's verified progress bar (`0% to 100%`).
  - Active session ID display (`SES-24EARCS001-XXXX`).
  - Camera & Geofence rules documentation.

### 3. Real-Time Camera Oval Verification & Geofencing
- Modal with live hardware webcam stream (`navigator.mediaDevices.getUserMedia`).
- White dashed oval face reticle with animated scanning beam.
- Checks hardware camera permission and facial framing.
- Verifies physical campus GPS boundary (accuracy ±15m) with automatic fallback.
- **Privacy Assurance:** HTML5 in-memory canvas capture verified instantaneously; **strictly 0 bytes written to disk**.

### 4. Bulk Student Sheet Upload (CSV / Excel)
- Accessible by both **Admin** and **Faculty**.
- **1-Click Template Download:** Generates `Extremis_Student_Template.csv` formatted with required headers:
  ```csv
  Full Name,Student Email ID,Branch,Section,Enrollment No.
  Aarav Sharma,aarav.cs001@extremis.edu,Computer Science & Engineering,A,24EARCS001
  Priya Rathore,priya.ad001@extremis.edu,Artificial Intelligence & Data Science,A,24EARAD001
  Rohan Verma,rohan.it001@extremis.edu,Information Technology,B,24EARIT001
  ```
- **Live In-Browser Parser:** Validates enrollment numbers, verifies sections, and renders an interactive preview table before import.
- **Automated Credential Provisioning:** Creates student records and matching login accounts (`Username = Enrollment No.`, `Password = student123`).

### 5. In-App Interactive Database Viewer
- Log in as **Admin** and select the **Database Viewer** tab.
- Live table inspection for:
  - `students`: Enrollment numbers, full names, official emails, branches, sections.
  - `attendance`: Date, check-in, check-out, and status records.
  - `sessions`: Active device session tokens and login timestamps.
  - `timetable`: Departmental schedules, rooms, and scan windows.
  - `audit_logs`: Security and biometric verification logs.
- **1-Click CSV Export:** Downloads any selected table directly to your computer as a formatted `.csv` spreadsheet.

---

## 🛠️ Technology Stack & Dependencies

| Layer | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java (OpenJDK) | 21 LTS | Core backend business logic and services |
| **Web Framework** | Javalin | 6.3.0 | Lightweight, non-blocking REST API web server |
| **HTTP Server** | Eclipse Jetty | 11.0.23 | Embedded HTTP/1.1 & WebSocket server |
| **Database (Cloud)** | PostgreSQL Driver | 42.7.4 | Production relational database support |
| **Database (Local)**| SQLite JDBC | 3.49.1.0 | Embedded zero-config local storage |
| **JSON Serialization**| Jackson Databind | 2.17.2 | High-performance JSON parsing and streaming |
| **Desktop UI Theme**| FlatLaf Mac Light | 3.5.4 | Native desktop Swing interface theme |
| **Frontend Styling**| Vanilla CSS3 | Modern | Design tokens, glassmorphism, responsive grid |
| **Frontend Logic** | Vanilla ES6+ JS | Modern | Dual-mode resilient client, webcam, canvas |
| **Build & Test** | Maven / JUnit 5 | 3.9+ / 5.10 | Dependency management & automated testing |

---

## 🚀 Getting Started & Local Setup

### Prerequisites
- **Java Development Kit (JDK) 21** or higher.
- **Apache Maven 3.8+**.
- Modern web browser (Chrome, Safari, Edge, Firefox) with webcam support.

### 1. Clone the Repository
```bash
git clone https://github.com/ChinmayBhattt/Extremis-Smart-Attendance-System.git
cd Extremis-Smart-Attendance-System
```

### 2. Build the Application
```bash
mvn clean package
```
*This compiles all 24 source files, runs all 12 automated unit tests, and creates a shaded Fat JAR in `target/extremis-attendance-1.0.0.jar`.*

### 3. Run with Local SQLite (Default)
```bash
java -jar target/extremis-attendance-1.0.0.jar
```
The application will launch on **`http://localhost:8080`** and automatically open in your default browser.

---

## 🐘 Configuring PostgreSQL (Cloud / Supabase / Neon)

To connect Extremis to an external PostgreSQL database, provide the standard `DATABASE_URL` environment variable:

```bash
# Example with cloud PostgreSQL (Supabase / Neon / AWS):
export DATABASE_URL="postgres://postgres:YourPassword@db.your-supabase-url.supabase.co:5432/postgres"

# Run application:
java -jar target/extremis-attendance-1.0.0.jar
```

Or configure via discrete parameters:
```bash
export DB_HOST="localhost"
export DB_PORT="5432"
export DB_NAME="extremis_db"
export DB_USER="postgres"
export DB_PASS="secret123"

java -jar target/extremis-attendance-1.0.0.jar
```

---

## 🧪 Automated Testing Suite

The repository contains an automated test suite verifying core business rules, single-session constraints, and data validations:

```bash
mvn test
```

### Test Coverage Highlights:
- ✅ Student registration, duplicate code detection, and field validation.
- ✅ Single-device session locking (blocking concurrent logins).
- ✅ Session release on logout and forced admin termination.
- ✅ Timetable query by department and slot conflict detection.
- ✅ Same-day duplicate check-in restrictions and check-out timestamping.

---

## 📁 Repository Structure

```
├── pom.xml                                   # Maven dependencies & build configuration
├── vercel.json                               # Vercel deployment configuration
├── README.md                                 # Project documentation
├── src
│   ├── main
│   │   ├── java/com/extremis
│   │   │   ├── Main.java                     # Application bootstrap & data seeder
│   │   │   ├── database
│   │   │   │   └── DatabaseManager.java      # Dual-engine PostgreSQL/SQLite connection manager
│   │   │   ├── model
│   │   │   │   ├── Student.java              # Student entity (code, name, email, branch, section)
│   │   │   │   ├── User.java                 # Authenticated user & session token model
│   │   │   │   ├── TimeTableEntry.java       # Department lecture schedule model
│   │   │   │   └── AttendanceRecord.java     # Check-in / Check-out log model
│   │   │   ├── dao                           # Data Access Objects (JDBC PreparedStatements)
│   │   │   │   ├── StudentDao.java
│   │   │   │   ├── UserDao.java
│   │   │   │   ├── TimeTableDao.java
│   │   │   │   └── AttendanceDao.java
│   │   │   ├── service                       # Business logic & security rules
│   │   │   │   ├── AuthService.java          # Single-device session lock engine
│   │   │   │   └── AttendanceService.java    # Lecture verification & check-ins
│   │   │   └── web
│   │   │       └── WebServer.java            # Javalin REST API endpoints & bulk import
│   │   └── resources
│   │       └── public                        # High-fidelity Web Frontend
│   │           ├── index.html                # Single-page responsive web application
│   │           ├── css/style.css             # Vanilla CSS design system & tokens
│   │           └── js/app.js                 # Resilient API client, webcam, and CSV parser
│   └── test
│       └── java/com/extremis
│           └── AttendanceServiceTest.java    # Automated unit test suite (12 tests)
```

---

## 📜 Project Details
 
- **Project:** Extremis Smart Attendance Management System
- **Subject:** Java Project-Based Learning (PBL)
- **Year:** 2026–2027
- **License:** MIT License — Open for academic demonstration and enhancement.

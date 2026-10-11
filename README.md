# Extremis — Smart Attendance for Every Lecture

**Project Type:** Java Project-Based Learning (PBL)  
**Academic Year:** 2026–2027  
**Technology Stack:** Java 21, Java Swing (Figma Design System + FlatMacLightLaf), SQLite JDBC, Maven, JUnit 5  
**Design Reference:** [Figma Design Assets in `/UI`](file:///Users/chinmaybhatt/javaPBL/UI)

---

## 🎨 1. Figma Design Implementation Overview

The application has been transformed to match the 7 official design mockups in [`/Users/chinmaybhatt/javaPBL/UI`](file:///Users/chinmaybhatt/javaPBL/UI) pixel-for-pixel:

| Mockup # | Screen | Implementation Details |
|:---|:---|:---|
| **1** | **Sign in (Student / Faculty / Admin)** | Split-screen layout. Left dark navy hero (`#0B1E36`) with "Smart attendance for every lecture". Right soft light panel with 3-tab capsule segment, inputs, and device policy box. |
| **2** | **Admin Students** | Navy sidebar with `E` Extremis logo. White table card with `ID`, `CODE`, `NAME`, `DEPARTMENT`. Actions: `Add Student`, `Edit Selected`, `Delete Selected`, `Refresh`. |
| **3** | **Admin Attendance** | Check-in/Check-out action card with green feedback banner (`Check-in recorded for EC2024-014 at 09:14 AM.`). Table with soft amber badge for `Not checked out`. |
| **4** | **Delete Confirmation Modal** | Clean modal with warning: `You are about to delete <Name> (<Code>). All of this student's attendance history will also be permanently deleted.` Outline `Cancel` and solid red `Delete Student`. |
| **5** | **Single-Device Block Modal** | Modal with `Already signed in on another device`. Buttons: `Contact Admin` and solid blue `OK`. |
| **6** | **Student Timetable** | Mobile/card dashboard. Top: `Hi, Aarav` & initials avatar `AS`. Vibrant blue hero card for active lecture (`Verification open`). Card list with `Verify now`, `Upcoming`, and `Verified` badges. |
| **7** | **Student Live Verification** | Top `Back` button. Dark navy camera card (`#0B1E36`) with white dashed face reticle (`Align your face inside the frame`). Checkboxes for camera and campus GPS with `Location: inside campus` badge. Bottom `Capture & Verify` button. |

---

## 🔑 2. Demonstration Accounts (From Figma Mockups)

| Role | Username / ID | Password | Name / Branch |
|:---|:---|:---|:---|
| 🎓 **Student** | `CS2024-001` | `student123` | Aarav Sharma (Computer Science) |
| 🎓 **Student** | `CS2024-002` | `student123` | Riya Verma (Computer Science) |
| 🎓 **Student** | `EC2024-014` | `student123` | Kabir Singh (Electronics) |
| 👨‍🏫 **Faculty** | `faculty.rajesh` | `faculty123` | Prof. A. Mehta (Computer Science) |
| 🛡️ **Admin** | `admin` | `admin123` | System Administrator (Full Access) |

---

## 🚀 3. How to Run the Application

```bash
# Direct run via Maven:
mvn compile exec:java -Dexec.mainClass="com.extremis.Main"

# Run the packaged standalone JAR:
java -jar target/extremis-attendance-1.0.0.jar
```

---

## 🧪 4. Automated Test Suite

All 12 automated acceptance tests are passing (100% test coverage):
```bash
mvn test
```
# Extremis-Smart-Attendance-System

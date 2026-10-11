package com.extremis;

import com.extremis.dao.AttendanceDao;
import com.extremis.dao.LectureAttendanceDao;
import com.extremis.dao.StudentDao;
import com.extremis.dao.TimeTableDao;
import com.extremis.dao.UserDao;
import com.extremis.database.DatabaseManager;
import com.extremis.model.TimeTableEntry;
import com.extremis.model.User;
import com.extremis.service.AttendanceService;
import com.extremis.service.AuthService;
import com.extremis.ui.LoginDialog;
import com.extremis.ui.UITheme;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.sql.SQLException;

/**
 * Extremis Application Entry Point.
 * Sets up FlatLaf modern UI, database, services, seeds multi-role accounts & timetables,
 * and launches the unified login portal.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize Modern Dark Dashboard Theme
        UITheme.setupTheme();

        // Initialize SQLite Database
        DatabaseManager dbManager = new DatabaseManager();
        try {
            dbManager.initializeDatabase();
            System.out.println("Extremis SQLite Database initialized: " + dbManager.getDbUrl());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Database initialization error:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        // Initialize DAOs & Services
        StudentDao studentDao = new StudentDao(dbManager);
        AttendanceDao attendanceDao = new AttendanceDao(dbManager);
        UserDao userDao = new UserDao(dbManager);
        TimeTableDao timeTableDao = new TimeTableDao(dbManager);
        LectureAttendanceDao lectureAttendanceDao = new LectureAttendanceDao(dbManager);

        AttendanceService attendanceService = new AttendanceService(
                studentDao, attendanceDao, timeTableDao, lectureAttendanceDao
        );
        AuthService authService = new AuthService(userDao);

        // Pre-seed sample accounts, timetables, and students if empty
        seedDataIfEmpty(attendanceService, userDao, timeTableDao);

        // Start Embedded Web Server
        int webPort = 8080;
        try {
            com.extremis.web.WebServer webServer = new com.extremis.web.WebServer(attendanceService, authService, webPort);
            webServer.start();

            // Try to open browser automatically
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:" + webPort));
                }
            } catch (Exception ex) {
                System.out.println("ℹ️ Open browser manually at: http://localhost:" + webPort);
            }
        } catch (Exception ex) {
            System.err.println("Could not start WebServer on port " + webPort + ": " + ex.getMessage());
        }

        // Check if --swing argument is passed to also open desktop window
        boolean launchSwing = false;
        for (String arg : args) {
            if ("--swing".equalsIgnoreCase(arg)) {
                launchSwing = true;
                break;
            }
        }
        if (launchSwing && !java.awt.GraphicsEnvironment.isHeadless()) {
            SwingUtilities.invokeLater(() -> {
                LoginDialog loginDialog = new LoginDialog(null, authService, attendanceService);
                loginDialog.setVisible(true);
            });
        }

        // Keep server process alive indefinitely
        try {
            while (true) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException ignored) {}
    }

    private static void seedDataIfEmpty(AttendanceService service, UserDao userDao, TimeTableDao timeTableDao) {
        try {
            // Seed Students matching Figma Mockups
            if (service.getAllStudents().isEmpty() || service.getStudentByCode("CS2024-001") == null) {
                if (service.getStudentByCode("CS2024-001") == null) {
                    service.registerStudent("CS2024-001", "Aarav Sharma", "Computer Science");
                    service.registerStudent("CS2024-002", "Riya Verma", "Computer Science");
                    service.registerStudent("EC2024-014", "Kabir Singh", "Electronics");
                    service.registerStudent("ME2024-007", "Ananya Gupta", "Mechanical");
                    service.registerStudent("CS2024-015", "Rohan Joshi", "Information Technology");
                    service.registerStudent("CV2024-021", "Meera Nair", "Civil");
                    System.out.println("✓ Figma mockup students seeded.");
                }
            }

            // Seed Users for all 3 Roles
            if (userDao.getUserByUsername("CS2024-001") == null) {
                userDao.addUser(new User(0, "admin", "admin123", "ADMIN", "System Administrator", "Central Administration", null, null, null));
                userDao.addUser(new User(0, "faculty.rajesh", "faculty123", "FACULTY", "Prof. A. Mehta", "Computer Science", null, null, null));
                userDao.addUser(new User(0, "CS2024-001", "student123", "STUDENT", "Aarav Sharma", "Computer Science", "CS2024-001", null, null));
                userDao.addUser(new User(0, "CS2024-002", "student123", "STUDENT", "Riya Verma", "Computer Science", "CS2024-002", null, null));
                userDao.addUser(new User(0, "EC2024-014", "student123", "STUDENT", "Kabir Singh", "Electronics", "EC2024-014", null, null));
                System.out.println("✓ Figma mockup users seeded.");
            }

            // Seed Branch Timetables matching Mockup 6
            if (timeTableDao.getTimeTableByDepartment("Computer Science").isEmpty()) {
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science", "Monday",
                        "Data Structures", "Prof. A. Mehta", "09:00", "10:00", "09:05", "Room 204"));
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science", "Monday",
                        "DBMS", "Prof. S. Kapoor", "10:00", "11:00", "10:05", "Room 205"));
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science", "Monday",
                        "Java Programming", "Prof. N. Rao", "11:15", "12:15", "11:20", "Lab 2"));
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science", "Monday",
                        "Mathematics III", "Prof. V. Iyer", "01:00", "02:00", "01:05", "Room 101"));
                System.out.println("✓ Figma mockup timetable seeded.");
            }
        } catch (Exception e) {
            System.err.println("Notice: Seeding skipped: " + e.getMessage());
        }
    }
}

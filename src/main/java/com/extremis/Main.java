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
            com.extremis.web.WebServer webServer = new com.extremis.web.WebServer(attendanceService, authService, dbManager, webPort);
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
            // Seed Students
            if (service.getStudentByCode("24EARCS001") == null) {
                service.registerStudent("24EARCS001", "Aarav Sharma", "Computer Science & Engineering", "aarav.cs001@extremis.edu", "A");
                service.registerStudent("24EARCS002", "Sneha Agarwal", "Computer Science & Engineering", "sneha.cs002@extremis.edu", "A");
                service.registerStudent("24EARAD001", "Priya Rathore", "Artificial Intelligence & Data Science", "priya.ad001@extremis.edu", "A");
                service.registerStudent("24EARAD002", "Devendra Singh", "Artificial Intelligence & Data Science", "devendra.ad002@extremis.edu", "B");
                service.registerStudent("24EARIT001", "Rohan Verma", "Information Technology", "rohan.it001@extremis.edu", "B");
                service.registerStudent("24EARIT002", "Anjali Meena", "Information Technology", "anjali.it002@extremis.edu", "A");
                System.out.println("✓ Extremis students seeded.");
            }

            // Seed Users for all Roles
            if (userDao.getUserByUsername("24EARCS001") == null) {
                userDao.addUser(new User(0, "admin", "admin123", "ADMIN", "System Administrator", "Extremis Administration", null, null, null));
                userDao.addUser(new User(0, "faculty.rajesh", "faculty123", "FACULTY", "Prof. Rajesh Kumar", "Computer Science & Engineering", null, null, null));
                userDao.addUser(new User(0, "24EARCS001", "student123", "STUDENT", "Aarav Sharma", "Computer Science & Engineering", "24EARCS001", null, null));
                userDao.addUser(new User(0, "24EARAD001", "student123", "STUDENT", "Priya Rathore", "Artificial Intelligence & Data Science", "24EARAD001", null, null));
                userDao.addUser(new User(0, "24EARIT001", "student123", "STUDENT", "Rohan Verma", "Information Technology", "24EARIT001", null, null));
                System.out.println("✓ Extremis users seeded.");
            }

            // Seed Branch Timetables matching Mockup 6
            if (timeTableDao.getTimeTableByDepartment("Computer Science & Engineering").isEmpty()) {
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science & Engineering", "Monday",
                        "Data Structures & Algorithms", "Prof. Rajesh Kumar", "09:00", "10:00", "09:05", "LT-102"));
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science & Engineering", "Monday",
                        "Database Management Systems", "Dr. Priya Nair", "10:00", "11:00", "10:05", "Lab 3B"));
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science & Engineering", "Monday",
                        "Object Oriented Java", "Prof. Vikram Sen", "11:15", "12:15", "11:20", "Lab 2A"));
                timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science & Engineering", "Monday",
                        "Discrete Mathematics", "Dr. Ananya Bose", "01:00", "02:00", "01:05", "Room 304"));
                System.out.println("✓ Extremis timetable seeded.");
            }
        } catch (Exception e) {
            System.err.println("Notice: Seeding skipped: " + e.getMessage());
        }
    }
}

package com.extremis.web;

import com.extremis.database.DatabaseManager;
import com.extremis.model.Attendance;
import com.extremis.model.Student;
import com.extremis.model.TimeTableEntry;
import com.extremis.model.User;
import com.extremis.service.AttendanceService;
import com.extremis.service.AuthService;
import com.extremis.service.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

/**
 * Embedded Javalin Web Server serving the REST API and the modern Web UI.
 */
public class WebServer {
    private final AttendanceService attendanceService;
    private final AuthService authService;
    private final DatabaseManager dbManager;
    private final int port;
    private Javalin app;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public WebServer(AttendanceService attendanceService, AuthService authService, DatabaseManager dbManager, int port) {
        this.attendanceService = attendanceService;
        this.authService = authService;
        this.dbManager = dbManager;
        this.port = port;
    }

    public WebServer(AttendanceService attendanceService, AuthService authService, int port) {
        this(attendanceService, authService, new DatabaseManager(), port);
    }

    public void start() {
        app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(MAPPER, false));

            // Serve static resources from classpath or filesystem for hot updates
            File devPublic = new File("src/main/resources/public");
            if (devPublic.exists() && devPublic.isDirectory()) {
                config.staticFiles.add(staticFiles -> {
                    staticFiles.hostedPath = "/";
                    staticFiles.directory = "src/main/resources/public";
                    staticFiles.location = Location.EXTERNAL;
                });
            } else {
                config.staticFiles.add(staticFiles -> {
                    staticFiles.hostedPath = "/";
                    staticFiles.directory = "/public";
                    staticFiles.location = Location.CLASSPATH;
                });
            }
        });

        registerRoutes();

        app.start(port);
        System.out.println("================================================================");
        System.out.println("  🌐 Extremis Web Application is LIVE on http://localhost:" + port);
        System.out.println("  🎨 Modern Web UI matching Figma designs ready!");
        System.out.println("================================================================");
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    private void registerRoutes() {
        // Health check
        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok", "app", "Extremis Web", "timestamp", System.currentTimeMillis())));

        // ==================== AUTHENTICATION ====================
        app.post("/api/auth/login", ctx -> {
            try {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String username = body.get("username");
                String password = body.get("password");
                String role = body.get("role");

                try {
                    User user = authService.login(username, password, role);
                    ctx.json(Map.of(
                            "success", true,
                            "user", Map.of(
                                    "id", user.getId(),
                                    "username", user.getUsername(),
                                    "role", user.getRole(),
                                    "fullName", user.getFullName() != null ? user.getFullName() : user.getUsername(),
                                    "department", user.getDepartment() != null ? user.getDepartment() : "",
                                    "studentCode", user.getStudentCode() != null ? user.getStudentCode() : "",
                                    "sessionId", user.getActiveSessionId() != null ? user.getActiveSessionId() : ""
                            )
                    ));
                } catch (ValidationException ve) {
                    if (ve.getMessage().contains("Single Session Policy Enforced")) {
                        // Return structured single-session lock response for Figma Modal 5
                        User existing = authService.getUserDao().getUserByUsername(username);
                        ctx.status(200).json(Map.of(
                                "success", false,
                                "blocked", true,
                                "message", "Already signed in on another device",
                                "detail", "Your student account is currently active on device " +
                                        (existing != null && existing.getActiveSessionId() != null ? existing.getActiveSessionId() : "Active Session") +
                                        ". Contact your admin if this wasn't you.",
                                "sessionId", existing != null && existing.getActiveSessionId() != null ? existing.getActiveSessionId() : "",
                                "loginTime", existing != null && existing.getSessionLoginTime() != null ? existing.getSessionLoginTime() : ""
                        ));
                    } else {
                        ctx.status(200).json(Map.of("success", false, "message", ve.getMessage()));
                    }
                }
            } catch (Exception e) {
                ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
            }
        });

        app.post("/api/auth/logout", ctx -> {
            try {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                int userId = ((Number) body.get("userId")).intValue();
                User user = authService.getUserDao().getUserById(userId);
                if (user != null) {
                    authService.logout(user);
                }
                ctx.json(Map.of("success", true, "message", "Signed out successfully."));
            } catch (Exception e) {
                ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
            }
        });

        // ==================== STUDENTS CRUD ====================
        app.get("/api/students", ctx -> {
            List<Student> students = attendanceService.getAllStudents();
            ctx.json(students);
        });

        app.post("/api/students", ctx -> {
            try {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String code = body.get("code");
                String name = body.get("name");
                String dept = body.get("department");

                Student created = attendanceService.registerStudent(code, name, dept);
                ctx.status(201).json(Map.of("success", true, "student", created));
            } catch (ValidationException ve) {
                ctx.status(400).json(Map.of("success", false, "message", ve.getMessage()));
            }
        });

        app.put("/api/students/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String code = body.get("code");
                String name = body.get("name");
                String dept = body.get("department");

                attendanceService.updateStudent(id, code, name, dept);
                ctx.json(Map.of("success", true, "message", "Student updated successfully"));
            } catch (ValidationException ve) {
                ctx.status(400).json(Map.of("success", false, "message", ve.getMessage()));
            }
        });

        app.delete("/api/students/{id}", ctx -> {
            try {
                int id = Integer.parseInt(ctx.pathParam("id"));
                attendanceService.deleteStudent(id);
                ctx.json(Map.of("success", true, "message", "Student deleted successfully"));
            } catch (ValidationException ve) {
                ctx.status(400).json(Map.of("success", false, "message", ve.getMessage()));
            }
        });

        // ==================== ATTENDANCE RECORDS ====================
        app.get("/api/attendance", ctx -> {
            String date = ctx.queryParam("date");
            List<Attendance> records = (date != null && !date.isEmpty())
                    ? attendanceService.getAttendanceByDate(date)
                    : attendanceService.getAllAttendanceRecords();
            ctx.json(records);
        });

        app.post("/api/attendance/check-in", ctx -> {
            try {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String studentCode = body.get("studentCode");
                Attendance record = attendanceService.recordCheckIn(studentCode);
                ctx.json(Map.of("success", true, "record", record, "message", "Check-in recorded for " + record.getStudentName() + " (" + record.getStudentCode() + ")"));
            } catch (ValidationException ve) {
                ctx.status(400).json(Map.of("success", false, "message", ve.getMessage()));
            }
        });

        app.post("/api/attendance/check-out", ctx -> {
            try {
                Map<String, String> body = ctx.bodyAsClass(Map.class);
                String studentCode = body.get("studentCode");
                Attendance record = attendanceService.recordCheckOut(studentCode);
                ctx.json(Map.of("success", true, "record", record, "message", "Check-out recorded for " + record.getStudentName() + " (" + record.getStudentCode() + ")"));
            } catch (ValidationException ve) {
                ctx.status(400).json(Map.of("success", false, "message", ve.getMessage()));
            }
        });

        // ==================== TIMETABLE & LECTURE VERIFICATION ====================
        app.get("/api/timetable", ctx -> {
            String dept = ctx.queryParam("department");
            String faculty = ctx.queryParam("faculty");

            List<TimeTableEntry> entries;
            if (dept != null && !dept.isEmpty()) {
                entries = attendanceService.getTimeTableForDepartment(dept);
            } else if (faculty != null && !faculty.isEmpty()) {
                entries = attendanceService.getTimeTableForFaculty(faculty);
            } else {
                entries = attendanceService.getAllTimetableEntries();
            }
            ctx.json(entries);
        });

        app.get("/api/student/verified-lectures", ctx -> {
            try {
                String sIdStr = ctx.queryParam("studentId");
                int studentId = (sIdStr != null && !sIdStr.isEmpty()) ? Integer.parseInt(sIdStr) : 0;
                String date = ctx.queryParam("date");
                if (date == null || date.isEmpty()) {
                    date = LocalDate.now().toString();
                }

                List<TimeTableEntry> allEntries = attendanceService.getAllTimetableEntries();
                List<Integer> verifiedIds = new ArrayList<>();
                for (TimeTableEntry entry : allEntries) {
                    if (attendanceService.hasStudentVerifiedLecture(entry.getId(), studentId, date)) {
                        verifiedIds.add(entry.getId());
                    }
                }
                ctx.json(Map.of("verifiedTimetableIds", verifiedIds, "date", date));
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });

        app.post("/api/student/verify-lecture", ctx -> {
            try {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                int timetableId = ((Number) body.get("timetableId")).intValue();
                int studentId = ((Number) body.get("studentId")).intValue();
                boolean cameraGranted = Boolean.TRUE.equals(body.get("cameraGranted"));
                boolean locationGranted = Boolean.TRUE.equals(body.get("locationGranted"));
                boolean photoCaptured = Boolean.TRUE.equals(body.get("photoCaptured"));

                // Note: Temporary photo is in-memory only, validated and NEVER written to disk
                boolean verified = attendanceService.verifyLectureAttendance(
                        timetableId, studentId, locationGranted, cameraGranted, photoCaptured
                );

                ctx.json(Map.of(
                        "success", verified,
                        "message", "Attendance verified successfully! (Temporary photo processed in-memory and discarded)"
                ));
            } catch (ValidationException ve) {
                ctx.status(400).json(Map.of("success", false, "message", ve.getMessage()));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
            }
        });

        // ==================== ADMIN SESSIONS MANAGEMENT ====================
        app.get("/api/admin/sessions", ctx -> {
            List<User> students = authService.getUserDao().getAllStudentsWithActiveSessions();
            List<Map<String, Object>> sessionList = new ArrayList<>();
            for (User u : students) {
                sessionList.add(Map.of(
                        "userId", u.getId(),
                        "username", u.getUsername(),
                        "fullName", u.getFullName(),
                        "department", u.getDepartment(),
                        "studentCode", u.getStudentCode(),
                        "sessionId", u.getActiveSessionId() != null ? u.getActiveSessionId() : "",
                        "loginTime", u.getSessionLoginTime() != null ? u.getSessionLoginTime() : ""
                ));
            }
            ctx.json(sessionList);
        });

        app.post("/api/admin/sessions/terminate", ctx -> {
            try {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                int userId = ((Number) body.get("userId")).intValue();
                authService.forceLogout(userId);
                ctx.json(Map.of("success", true, "message", "Session terminated. Student can now log in on a new device."));
            } catch (Exception e) {
                ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
            }
        });

        // ==================== STATS OVERVIEW ====================
        app.get("/api/stats", ctx -> {
            int totalStudents = attendanceService.getAllStudents().size();
            String today = LocalDate.now().toString();
            List<Attendance> todayRecords = attendanceService.getAttendanceByDate(today);
            int checkedInToday = todayRecords.size();
            long checkedOutToday = todayRecords.stream().filter(a -> a.getCheckOut() != null).count();
            int activeSessions = authService.getUserDao().getAllStudentsWithActiveSessions().size();

            ctx.json(Map.of(
                    "totalStudents", totalStudents,
                    "checkedInToday", checkedInToday,
                    "checkedOutToday", checkedOutToday,
                    "activeSessions", activeSessions,
                    "todayDate", today
            ));
        });

        // ==================== DATABASE EXPLORER ====================
        app.get("/api/database/info", ctx -> {
            ctx.json(Map.of(
                    "type", dbManager.getDatabaseType(),
                    "url", dbManager.getSanitizedDbUrl(),
                    "tables", List.of("students", "attendance", "users", "timetable", "lecture_attendance")
            ));
        });

        app.get("/api/database/inspect", ctx -> {
            String table = ctx.queryParam("table");
            if (table == null || table.isEmpty()) table = "students";
            try {
                Map<String, Object> data = dbManager.inspectTable(table);
                ctx.json(data);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });
    }
}

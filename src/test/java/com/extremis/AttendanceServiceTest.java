package com.extremis;

import com.extremis.dao.*;
import com.extremis.database.DatabaseManager;
import com.extremis.model.*;
import com.extremis.service.AttendanceService;
import com.extremis.service.AuthService;
import com.extremis.service.ValidationException;
import org.junit.jupiter.api.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated test suite covering Student CRUD, Attendance rules,
 * Multi-Role Authentication, Student Single-Session Lock, and Real-Time Lecture Verification.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AttendanceServiceTest {

    private static File tempDbFile;
    private static DatabaseManager dbManager;
    private static StudentDao studentDao;
    private static AttendanceDao attendanceDao;
    private static UserDao userDao;
    private static TimeTableDao timeTableDao;
    private static LectureAttendanceDao lectureAttendanceDao;
    private static AttendanceService service;
    private static AuthService authService;

    @BeforeAll
    public static void setUp() throws Exception {
        tempDbFile = File.createTempFile("extremis_test_", ".db");
        tempDbFile.deleteOnExit();

        dbManager = new DatabaseManager("jdbc:sqlite:" + tempDbFile.getAbsolutePath());
        dbManager.initializeDatabase();

        studentDao = new StudentDao(dbManager);
        attendanceDao = new AttendanceDao(dbManager);
        userDao = new UserDao(dbManager);
        timeTableDao = new TimeTableDao(dbManager);
        lectureAttendanceDao = new LectureAttendanceDao(dbManager);

        service = new AttendanceService(studentDao, attendanceDao, timeTableDao, lectureAttendanceDao);
        authService = new AuthService(userDao);
    }

    @AfterAll
    public static void tearDown() {
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    @Test
    @Order(1)
    @DisplayName("T-01: Multi-Role Authentication & Seeding")
    public void testMultiRoleAuthentication() throws SQLException {
        // Add users for all 3 roles
        userDao.addUser(new User(0, "admin_test", "admin123", "ADMIN", "Test Admin", "Administration", null, null, null));
        userDao.addUser(new User(0, "faculty_test", "faculty123", "FACULTY", "Prof. Test", "CSE", null, null, null));
        userDao.addUser(new User(0, "STU-001", "student123", "STUDENT", "Alice Smith", "CSE", "STU-001", null, null));

        // Test successful logins
        User admin = authService.login("admin_test", "admin123", "ADMIN");
        assertNotNull(admin);
        assertEquals("ADMIN", admin.getRole());

        User faculty = authService.login("faculty_test", "faculty123", "FACULTY");
        assertNotNull(faculty);
        assertEquals("FACULTY", faculty.getRole());

        // Incorrect password fails
        assertThrows(ValidationException.class, () -> authService.login("admin_test", "wrongpass", "ADMIN"));
    }

    @Test
    @Order(2)
    @DisplayName("T-02: Student Single-Session Lock Enforcement")
    public void testStudentSingleSessionLock() throws SQLException {
        // First login succeeds and assigns active session token
        User studentLogin1 = authService.login("STU-001", "student123", "STUDENT");
        assertNotNull(studentLogin1);
        assertNotNull(studentLogin1.getActiveSessionId());
        assertTrue(studentLogin1.getActiveSessionId().startsWith("SES-"));

        // Second simultaneous login attempt for the same student MUST fail
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            authService.login("STU-001", "student123", "STUDENT");
        });
        assertTrue(ex.getMessage().contains("Single Session Policy Enforced"));

        // Logging out clears the active session
        authService.logout(studentLogin1);
        assertNull(studentLogin1.getActiveSessionId());

        // Now login can succeed again
        User studentLogin2 = authService.login("STU-001", "student123", "STUDENT");
        assertNotNull(studentLogin2.getActiveSessionId());
        authService.logout(studentLogin2);
    }

    @Test
    @Order(3)
    @DisplayName("T-03: Add a valid student")
    public void testAddValidStudent() throws SQLException {
        Student student = service.registerStudent("TEST-001", "Alice Smith", "Computer Science");
        assertNotNull(student);
        assertTrue(student.getId() > 0);
        assertEquals("TEST-001", student.getCode());
        assertEquals("Alice Smith", student.getName());

        Student fetched = service.getStudentByCode("TEST-001");
        assertNotNull(fetched);
        assertEquals("Alice Smith", fetched.getName());
    }

    @Test
    @Order(4)
    @DisplayName("T-04: Add duplicate student code is rejected")
    public void testAddDuplicateStudentCodeRejected() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            service.registerStudent("TEST-001", "Duplicate Alice", "Information Technology");
        });
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @Order(5)
    @DisplayName("T-05: Submit blank student fields is rejected")
    public void testBlankStudentFieldsRejected() {
        assertThrows(ValidationException.class, () -> service.registerStudent("", "Bob", "CSE"));
        assertThrows(ValidationException.class, () -> service.registerStudent("   ", "Bob", "CSE"));
        assertThrows(ValidationException.class, () -> service.registerStudent("TEST-002", "", "CSE"));
        assertThrows(ValidationException.class, () -> service.registerStudent("TEST-002", "Bob", ""));
    }

    @Test
    @Order(6)
    @DisplayName("T-06: Edit selected student")
    public void testEditStudent() throws SQLException {
        Student bob = service.registerStudent("TEST-002", "Bob Brown", "Mechanical");
        service.updateStudent(bob.getId(), "TEST-002-MOD", "Bob B. Brown", "Mechatronics");

        Student updated = service.getStudentByCode("TEST-002-MOD");
        assertNotNull(updated);
        assertEquals("Bob B. Brown", updated.getName());
        assertEquals("Mechatronics", updated.getDepartment());
    }

    @Test
    @Order(7)
    @DisplayName("T-07: Branch Timetable Retrieval & Lecture Verification")
    public void testTimetableAndLectureVerification() throws SQLException {
        // Add sample timetable entry
        timeTableDao.addEntry(new TimeTableEntry(0, "Computer Science", "Monday",
                "Advanced Java", "Dr. Rajesh", "09:00", "10:00", "09:05", "Room 301"));

        List<TimeTableEntry> entries = service.getTimeTableForDepartment("Computer Science");
        assertFalse(entries.isEmpty());
        TimeTableEntry lecture = entries.get(0);
        assertEquals("Advanced Java", lecture.getSubject());

        Student alice = service.getStudentByCode("TEST-001");

        // Verify without GPS should fail
        assertThrows(ValidationException.class, () -> {
            service.verifyLectureAttendance(lecture.getId(), alice.getId(), false, true, new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        });

        // Verify without Camera should fail
        assertThrows(ValidationException.class, () -> {
            service.verifyLectureAttendance(lecture.getId(), alice.getId(), true, false, new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB));
        });

        // Verify without temporary photo should fail
        assertThrows(ValidationException.class, () -> {
            service.verifyLectureAttendance(lecture.getId(), alice.getId(), true, true, null);
        });

        // Valid verification with in-memory photo (never written to disk)
        BufferedImage memPhoto = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        boolean verified = service.verifyLectureAttendance(lecture.getId(), alice.getId(), true, true, memPhoto);
        assertTrue(verified);

        // Verification record exists in DB
        assertTrue(service.hasStudentVerifiedLecture(lecture.getId(), alice.getId(), LocalDate.now().toString()));

        // Duplicate verification for the same lecture today must be rejected
        assertThrows(ValidationException.class, () -> {
            service.verifyLectureAttendance(lecture.getId(), alice.getId(), true, true, memPhoto);
        });
    }

    @Test
    @Order(8)
    @DisplayName("T-08: Check in a registered student")
    public void testCheckInRegisteredStudent() throws SQLException {
        Attendance record = service.recordCheckIn("TEST-001");
        assertNotNull(record);
        assertEquals("TEST-001", record.getStudentCode());
        assertEquals(LocalDate.now().toString(), record.getDate());
        assertNotNull(record.getCheckIn());
        assertNull(record.getCheckOut());
        assertFalse(record.isCheckedOut());
    }

    @Test
    @Order(9)
    @DisplayName("T-09: Duplicate check-in on the same day is rejected")
    public void testDuplicateCheckInRejected() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            service.recordCheckIn("TEST-001");
        });
        assertTrue(ex.getMessage().contains("already checked in today"));
    }

    @Test
    @Order(10)
    @DisplayName("T-10: Check out after check-in")
    public void testCheckOutAfterCheckIn() throws SQLException {
        Attendance record = service.recordCheckOut("TEST-001");
        assertNotNull(record);
        assertNotNull(record.getCheckOut());
        assertTrue(record.isCheckedOut());
    }

    @Test
    @Order(11)
    @DisplayName("T-11: Check out without an open record is rejected")
    public void testCheckOutWithoutOpenRecordRejected() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            service.recordCheckOut("TEST-001");
        });
        assertTrue(ex.getMessage().contains("already checked out today"));

        ValidationException ex2 = assertThrows(ValidationException.class, () -> {
            service.recordCheckOut("TEST-002-MOD");
        });
        assertTrue(ex2.getMessage().contains("No check-in record found"));
    }

    @Test
    @Order(12)
    @DisplayName("T-12: Delete student and cascade delete attendance")
    public void testDeleteStudentCascade() throws SQLException {
        Student alice = service.getStudentByCode("TEST-001");
        assertNotNull(alice);

        service.deleteStudent(alice.getId());
        assertNull(service.getStudentByCode("TEST-001"));

        List<Attendance> after = service.getAttendanceByStudentCode("TEST-001");
        assertTrue(after.isEmpty());
    }
}

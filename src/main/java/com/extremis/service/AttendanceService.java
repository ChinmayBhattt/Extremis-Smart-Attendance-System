package com.extremis.service;

import com.extremis.dao.AttendanceDao;
import com.extremis.dao.LectureAttendanceDao;
import com.extremis.dao.StudentDao;
import com.extremis.dao.TimeTableDao;
import com.extremis.model.Attendance;
import com.extremis.model.LectureAttendance;
import com.extremis.model.Student;
import com.extremis.model.TimeTableEntry;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service orchestrating student registration, attendance workflows,
 * branch timetables, lecture-wise real-time photo verification, and business rules.
 */
public class AttendanceService {
    private final StudentDao studentDao;
    private final AttendanceDao attendanceDao;
    private final TimeTableDao timeTableDao;
    private final LectureAttendanceDao lectureAttendanceDao;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AttendanceService(StudentDao studentDao, AttendanceDao attendanceDao) {
        this(studentDao, attendanceDao, null, null);
    }

    public AttendanceService(StudentDao studentDao, AttendanceDao attendanceDao,
                             TimeTableDao timeTableDao, LectureAttendanceDao lectureAttendanceDao) {
        this.studentDao = studentDao;
        this.attendanceDao = attendanceDao;
        this.timeTableDao = timeTableDao;
        this.lectureAttendanceDao = lectureAttendanceDao;
    }

    // ==================== STUDENT CRUD ====================
    public Student registerStudent(String code, String name, String department) throws SQLException {
        return registerStudent(code, name, department, "", "A");
    }

    public Student registerStudent(String code, String name, String department, String email, String section) throws SQLException {
        validateStudentFields(code, name, department);

        if (studentDao.studentCodeExists(code.trim(), null)) {
            throw new ValidationException("A student with code '" + code.trim() + "' already exists.");
        }

        Student student = new Student(0, code.trim(), name.trim(), department.trim(), email, section);
        return studentDao.addStudent(student);
    }

    public void updateStudent(int id, String code, String name, String department) throws SQLException {
        updateStudent(id, code, name, department, "", "A");
    }

    public void updateStudent(int id, String code, String name, String department, String email, String section) throws SQLException {
        Student existing = studentDao.getStudentById(id);
        if (existing == null) {
            throw new ValidationException("Student with ID " + id + " does not exist.");
        }

        validateStudentFields(code, name, department);

        if (studentDao.studentCodeExists(code.trim(), id)) {
            throw new ValidationException("Another student with code '" + code.trim() + "' already exists.");
        }

        existing.setCode(code.trim());
        existing.setName(name.trim());
        existing.setDepartment(department.trim());
        existing.setEmail(email != null ? email.trim() : "");
        existing.setSection(section != null ? section.trim() : "A");

        studentDao.updateStudent(existing);
    }

    public void deleteStudent(int id) throws SQLException {
        Student student = studentDao.getStudentById(id);
        if (student == null) {
            throw new ValidationException("Student with ID " + id + " was not found.");
        }
        studentDao.deleteStudent(id);
    }

    public List<Student> getAllStudents() throws SQLException {
        return studentDao.getAllStudents();
    }

    public Student getStudentByCode(String code) throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return studentDao.getStudentByCode(code.trim());
    }

    public Student getStudentById(int id) throws SQLException {
        return studentDao.getStudentById(id);
    }

    // ==================== DAILY CHECK-IN / CHECK-OUT ====================
    public Attendance recordCheckIn(String studentCode) throws SQLException {
        if (studentCode == null || studentCode.trim().isEmpty()) {
            throw new ValidationException("Please enter a student code.");
        }

        String trimmedCode = studentCode.trim();
        Student student = studentDao.getStudentByCode(trimmedCode);
        if (student == null) {
            throw new ValidationException("Student with code '" + trimmedCode + "' not found. Please register the student first.");
        }

        String today = LocalDate.now().toString();
        String nowTime = LocalTime.now().format(TIME_FORMATTER);

        Attendance existingRecord = attendanceDao.getAttendanceRecord(student.getId(), today);
        if (existingRecord != null) {
            throw new ValidationException(String.format(
                    "Duplicate Check-In: Student '%s' (%s) has already checked in today (%s) at %s.",
                    student.getName(), student.getCode(), today, existingRecord.getCheckIn()
            ));
        }

        Attendance attendance = attendanceDao.recordCheckIn(student.getId(), today, nowTime);
        attendance.setStudentCode(student.getCode());
        attendance.setStudentName(student.getName());
        attendance.setDepartment(student.getDepartment());
        return attendance;
    }

    public Attendance recordCheckOut(String studentCode) throws SQLException {
        if (studentCode == null || studentCode.trim().isEmpty()) {
            throw new ValidationException("Please enter a student code.");
        }

        String trimmedCode = studentCode.trim();
        Student student = studentDao.getStudentByCode(trimmedCode);
        if (student == null) {
            throw new ValidationException("Student with code '" + trimmedCode + "' not found.");
        }

        String today = LocalDate.now().toString();
        String nowTime = LocalTime.now().format(TIME_FORMATTER);

        Attendance attendanceRecord = attendanceDao.getAttendanceRecord(student.getId(), today);
        if (attendanceRecord == null) {
            throw new ValidationException(String.format(
                    "No check-in record found for student '%s' (%s) on %s. Check-in must be recorded before check-out.",
                    student.getName(), student.getCode(), today
            ));
        }

        if (attendanceRecord.isCheckedOut()) {
            throw new ValidationException(String.format(
                    "Student '%s' (%s) has already checked out today at %s.",
                    student.getName(), student.getCode(), attendanceRecord.getCheckOut()
            ));
        }

        boolean updated = attendanceDao.recordCheckOut(attendanceRecord.getId(), nowTime);
        if (!updated) {
            throw new ValidationException("Could not update check-out status. Please try again.");
        }

        attendanceRecord.setCheckOut(nowTime);
        return attendanceRecord;
    }

    public List<Attendance> getAllAttendanceRecords() throws SQLException {
        return attendanceDao.getAllAttendanceRecords();
    }

    public List<Attendance> getAttendanceByDate(String date) throws SQLException {
        if (date == null || date.trim().isEmpty()) {
            return getAllAttendanceRecords();
        }
        return attendanceDao.getAttendanceByDate(date.trim());
    }

    public List<Attendance> getAttendanceByStudentCode(String code) throws SQLException {
        if (code == null || code.trim().isEmpty()) {
            return getAllAttendanceRecords();
        }
        return attendanceDao.getAttendanceByStudentCode(code.trim());
    }

    // ==================== TIMETABLE & REALTIME LECTURE ATTENDANCE ====================
    public List<TimeTableEntry> getTimeTableForDepartment(String department) throws SQLException {
        if (timeTableDao == null) return List.of();
        return timeTableDao.getTimeTableByDepartment(department);
    }

    public List<TimeTableEntry> getTimeTableForFaculty(String facultyName) throws SQLException {
        if (timeTableDao == null) return List.of();
        return timeTableDao.getTimeTableForFaculty(facultyName);
    }

    public List<TimeTableEntry> getAllTimetableEntries() throws SQLException {
        if (timeTableDao == null) return List.of();
        return timeTableDao.getAllEntries();
    }

    /**
     * Verifies real-time attendance for a lecture.
     * Note: As specified, the captured photo is validated in memory temporarily and is NEVER saved to disk.
     */
    public boolean verifyLectureAttendance(int timetableId, int studentId,
                                           boolean locationGranted, boolean cameraGranted,
                                           BufferedImage temporaryPhoto) throws SQLException {
        if (!locationGranted) {
            throw new ValidationException("Verification Failed: GPS Location permission is mandatory to verify you are on college campus!");
        }
        if (!cameraGranted) {
            throw new ValidationException("Verification Failed: Live Camera permission is mandatory to capture verification snapshot!");
        }
        if (temporaryPhoto == null) {
            throw new ValidationException("Verification Failed: Real-time photo snapshot must be captured to prove presence!");
        }

        if (lectureAttendanceDao == null) {
            throw new ValidationException("Lecture attendance module not initialized.");
        }

        String today = LocalDate.now().toString();
        if (lectureAttendanceDao.hasStudentVerified(timetableId, studentId, today)) {
            throw new ValidationException("Duplicate Verification: You have already verified attendance for this lecture today!");
        }

        String nowTime = LocalTime.now().format(TIME_FORMATTER);

        // Record verification in SQLite (photo remains strictly temporary in memory, never saved to disk)
        return lectureAttendanceDao.recordLectureAttendance(
                timetableId, studentId, today, nowTime, "PRESENT", true, true
        );
    }

    public boolean verifyLectureAttendance(int timetableId, int studentId,
                                           boolean locationGranted, boolean cameraGranted,
                                           boolean photoCaptured) throws SQLException {
        if (!locationGranted) {
            throw new ValidationException("Verification Failed: GPS Location permission is mandatory to verify you are on college campus!");
        }
        if (!cameraGranted) {
            throw new ValidationException("Verification Failed: Live Camera permission is mandatory to capture verification snapshot!");
        }
        if (!photoCaptured) {
            throw new ValidationException("Verification Failed: Real-time photo snapshot must be captured to prove presence!");
        }

        if (lectureAttendanceDao == null) {
            throw new ValidationException("Lecture attendance module not initialized.");
        }

        String today = LocalDate.now().toString();
        if (lectureAttendanceDao.hasStudentVerified(timetableId, studentId, today)) {
            throw new ValidationException("Duplicate Verification: You have already verified attendance for this lecture today!");
        }

        String nowTime = LocalTime.now().format(TIME_FORMATTER);

        return lectureAttendanceDao.recordLectureAttendance(
                timetableId, studentId, today, nowTime, "PRESENT", true, true
        );
    }

    public boolean hasStudentVerifiedLecture(int timetableId, int studentId, String date) throws SQLException {
        if (lectureAttendanceDao == null) return false;
        return lectureAttendanceDao.hasStudentVerified(timetableId, studentId, date);
    }

    public List<LectureAttendance> getLectureAttendanceRoster(int timetableId, String date) throws SQLException {
        if (lectureAttendanceDao == null) return List.of();
        return lectureAttendanceDao.getAttendanceForLectureDate(timetableId, date);
    }

    public List<LectureAttendance> getStudentLectureAttendanceHistory(int studentId) throws SQLException {
        if (lectureAttendanceDao == null) return List.of();
        return lectureAttendanceDao.getStudentLectureAttendance(studentId);
    }

    public List<LectureAttendance> getAllLectureAttendanceRecords() throws SQLException {
        if (lectureAttendanceDao == null) return List.of();
        return lectureAttendanceDao.getAllLectureAttendance();
    }

    private void validateStudentFields(String code, String name, String department) {
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Student Code is required and cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Student Name is required and cannot be empty.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new ValidationException("Department is required and cannot be empty.");
        }
    }
}

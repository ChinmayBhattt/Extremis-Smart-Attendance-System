package com.extremis.dao;

import com.extremis.database.DatabaseManager;
import com.extremis.model.Attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance entity operations.
 */
public class AttendanceDao {
    private final DatabaseManager dbManager;

    public AttendanceDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Records a check-in for a student on a specific date.
     * Enforces the UNIQUE(student_id, date) constraint at database level.
     */
    public Attendance recordCheckIn(int studentId, String date, String checkInTime) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, date, check_in) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, date);
            pstmt.setString(3, checkInTime);
            pstmt.executeUpdate();

            int generatedId = -1;
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }
            return new Attendance(generatedId, studentId, date, checkInTime, null);
        }
    }

    /**
     * Checks if a student has already checked in on a specific date.
     */
    public boolean hasCheckedInToday(int studentId, String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND date = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Retrieves an open attendance record (check-in exists, but check_out is null or empty) for a student on a date.
     */
    public Attendance getOpenAttendance(int studentId, String date) throws SQLException {
        String sql = """
            SELECT a.id, a.student_id, a.date, a.check_in, a.check_out,
                   s.code AS student_code, s.name AS student_name, s.department
            FROM attendance a
            JOIN students s ON a.student_id = s.id
            WHERE a.student_id = ? AND a.date = ? AND (a.check_out IS NULL OR a.check_out = '')
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves attendance record for a student on a specific date (open or closed).
     */
    public Attendance getAttendanceRecord(int studentId, String date) throws SQLException {
        String sql = """
            SELECT a.id, a.student_id, a.date, a.check_in, a.check_out,
                   s.code AS student_code, s.name AS student_name, s.department
            FROM attendance a
            JOIN students s ON a.student_id = s.id
            WHERE a.student_id = ? AND a.date = ?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setString(2, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
        }
        return null;
    }

    /**
     * Records check-out time for a specific attendance record.
     */
    public boolean recordCheckOut(int attendanceId, String checkOutTime) throws SQLException {
        String sql = "UPDATE attendance SET check_out = ? WHERE id = ? AND (check_out IS NULL OR check_out = '')";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, checkOutTime);
            pstmt.setInt(2, attendanceId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all attendance records with student details, ordered by date DESC and check_in DESC.
     */
    public List<Attendance> getAllAttendanceRecords() throws SQLException {
        List<Attendance> records = new ArrayList<>();
        String sql = """
            SELECT a.id, a.student_id, a.date, a.check_in, a.check_out,
                   s.code AS student_code, s.name AS student_name, s.department
            FROM attendance a
            JOIN students s ON a.student_id = s.id
            ORDER BY a.date DESC, a.check_in DESC
        """;
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                records.add(mapResultSetToAttendance(rs));
            }
        }
        return records;
    }

    /**
     * Retrieves attendance records filtered by date.
     */
    public List<Attendance> getAttendanceByDate(String date) throws SQLException {
        List<Attendance> records = new ArrayList<>();
        String sql = """
            SELECT a.id, a.student_id, a.date, a.check_in, a.check_out,
                   s.code AS student_code, s.name AS student_name, s.department
            FROM attendance a
            JOIN students s ON a.student_id = s.id
            WHERE a.date = ?
            ORDER BY a.check_in DESC
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToAttendance(rs));
                }
            }
        }
        return records;
    }

    /**
     * Retrieves attendance records filtered by student code.
     */
    public List<Attendance> getAttendanceByStudentCode(String studentCode) throws SQLException {
        List<Attendance> records = new ArrayList<>();
        String sql = """
            SELECT a.id, a.student_id, a.date, a.check_in, a.check_out,
                   s.code AS student_code, s.name AS student_name, s.department
            FROM attendance a
            JOIN students s ON a.student_id = s.id
            WHERE UPPER(s.code) = UPPER(?)
            ORDER BY a.date DESC, a.check_in DESC
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToAttendance(rs));
                }
            }
        }
        return records;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance(
                rs.getInt("id"),
                rs.getInt("student_id"),
                rs.getString("date"),
                rs.getString("check_in"),
                rs.getString("check_out")
        );
        attendance.setStudentCode(rs.getString("student_code"));
        attendance.setStudentName(rs.getString("student_name"));
        attendance.setDepartment(rs.getString("department"));
        return attendance;
    }
}

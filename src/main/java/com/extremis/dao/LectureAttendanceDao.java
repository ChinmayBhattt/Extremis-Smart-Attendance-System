package com.extremis.dao;

import com.extremis.database.DatabaseManager;
import com.extremis.model.LectureAttendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for real-time lecture attendance records.
 */
public class LectureAttendanceDao {
    private final DatabaseManager dbManager;

    public LectureAttendanceDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Records real-time attendance for a student for a specific lecture.
     */
    public boolean recordLectureAttendance(int timetableId, int studentId, String date,
                                           String verifiedTime, String status,
                                           boolean locationVerified, boolean cameraVerified) throws SQLException {
        String sql = """
            INSERT INTO lecture_attendance (timetable_id, student_id, date, verified_time, status, location_verified, camera_verified)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, timetableId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, date);
            pstmt.setString(4, verifiedTime);
            pstmt.setString(5, status);
            pstmt.setInt(6, locationVerified ? 1 : 0);
            pstmt.setInt(7, cameraVerified ? 1 : 0);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a student has already verified attendance for a lecture on a given date.
     */
    public boolean hasStudentVerified(int timetableId, int studentId, String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM lecture_attendance WHERE timetable_id = ? AND student_id = ? AND date = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, timetableId);
            pstmt.setInt(2, studentId);
            pstmt.setString(3, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Gets lecture attendance roster for a specific timetable lecture on a given date.
     */
    public List<LectureAttendance> getAttendanceForLectureDate(int timetableId, String date) throws SQLException {
        List<LectureAttendance> list = new ArrayList<>();
        String sql = """
            SELECT la.id, la.timetable_id, la.student_id, la.date, la.verified_time, la.status,
                   la.location_verified, la.camera_verified,
                   s.code AS student_code, s.name AS student_name, s.department,
                   t.subject
            FROM lecture_attendance la
            JOIN students s ON la.student_id = s.id
            JOIN timetable t ON la.timetable_id = t.id
            WHERE la.timetable_id = ? AND la.date = ?
            ORDER BY la.verified_time ASC
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, timetableId);
            pstmt.setString(2, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all lecture attendance history for a given student.
     */
    public List<LectureAttendance> getStudentLectureAttendance(int studentId) throws SQLException {
        List<LectureAttendance> list = new ArrayList<>();
        String sql = """
            SELECT la.id, la.timetable_id, la.student_id, la.date, la.verified_time, la.status,
                   la.location_verified, la.camera_verified,
                   s.code AS student_code, s.name AS student_name, s.department,
                   t.subject
            FROM lecture_attendance la
            JOIN students s ON la.student_id = s.id
            JOIN timetable t ON la.timetable_id = t.id
            WHERE la.student_id = ?
            ORDER BY la.date DESC, la.verified_time DESC
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all lecture attendance records college-wide.
     */
    public List<LectureAttendance> getAllLectureAttendance() throws SQLException {
        List<LectureAttendance> list = new ArrayList<>();
        String sql = """
            SELECT la.id, la.timetable_id, la.student_id, la.date, la.verified_time, la.status,
                   la.location_verified, la.camera_verified,
                   s.code AS student_code, s.name AS student_name, s.department,
                   t.subject
            FROM lecture_attendance la
            JOIN students s ON la.student_id = s.id
            JOIN timetable t ON la.timetable_id = t.id
            ORDER BY la.date DESC, la.verified_time DESC
        """;
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    private LectureAttendance mapResultSet(ResultSet rs) throws SQLException {
        return new LectureAttendance(
                rs.getInt("id"),
                rs.getInt("timetable_id"),
                rs.getInt("student_id"),
                rs.getString("student_code"),
                rs.getString("student_name"),
                rs.getString("department"),
                rs.getString("subject"),
                rs.getString("date"),
                rs.getString("verified_time"),
                rs.getString("status"),
                rs.getInt("location_verified") == 1,
                rs.getInt("camera_verified") == 1
        );
    }
}

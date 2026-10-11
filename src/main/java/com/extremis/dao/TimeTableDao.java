package com.extremis.dao;

import com.extremis.database.DatabaseManager;
import com.extremis.model.TimeTableEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for branch timetables and lecture schedules.
 */
public class TimeTableDao {
    private final DatabaseManager dbManager;

    public TimeTableDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieves timetable entries for a specific department/branch.
     */
    public List<TimeTableEntry> getTimeTableByDepartment(String department) throws SQLException {
        List<TimeTableEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM timetable WHERE UPPER(department) = UPPER(?) ORDER BY start_time ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, department.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves timetable entries for a specific faculty member.
     */
    public List<TimeTableEntry> getTimeTableForFaculty(String facultyName) throws SQLException {
        List<TimeTableEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM timetable WHERE UPPER(faculty_name) LIKE UPPER(?) ORDER BY start_time ASC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + facultyName.trim() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Retrieves all timetable entries across all branches.
     */
    public List<TimeTableEntry> getAllEntries() throws SQLException {
        List<TimeTableEntry> list = new ArrayList<>();
        String sql = "SELECT * FROM timetable ORDER BY department, start_time ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Retrieves a timetable entry by ID.
     */
    public TimeTableEntry getEntryById(int id) throws SQLException {
        String sql = "SELECT * FROM timetable WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Adds a timetable entry.
     */
    public boolean addEntry(TimeTableEntry entry) throws SQLException {
        String sql = """
            INSERT INTO timetable (department, day_of_week, subject, faculty_name, start_time, end_time, verification_time, room_number)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, entry.getDepartment());
            pstmt.setString(2, entry.getDayOfWeek());
            pstmt.setString(3, entry.getSubject());
            pstmt.setString(4, entry.getFacultyName());
            pstmt.setString(5, entry.getStartTime());
            pstmt.setString(6, entry.getEndTime());
            pstmt.setString(7, entry.getVerificationTime());
            pstmt.setString(8, entry.getRoomNumber());
            return pstmt.executeUpdate() > 0;
        }
    }

    private TimeTableEntry mapResultSet(ResultSet rs) throws SQLException {
        return new TimeTableEntry(
                rs.getInt("id"),
                rs.getString("department"),
                rs.getString("day_of_week"),
                rs.getString("subject"),
                rs.getString("faculty_name"),
                rs.getString("start_time"),
                rs.getString("end_time"),
                rs.getString("verification_time"),
                rs.getString("room_number")
        );
    }
}

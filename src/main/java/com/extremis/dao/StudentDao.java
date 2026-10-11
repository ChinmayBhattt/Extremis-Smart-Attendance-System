package com.extremis.dao;

import com.extremis.database.DatabaseManager;
import com.extremis.model.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student entity operations.
 */
public class StudentDao {
    private final DatabaseManager dbManager;

    public StudentDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Adds a new student to the database.
     * @param student Student object to insert.
     * @return inserted student with generated ID.
     */
    public Student addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (code, name, department) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, student.getCode().trim());
            pstmt.setString(2, student.getName().trim());
            pstmt.setString(3, student.getDepartment().trim());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    student.setId(rs.getInt(1));
                }
            }
            return student;
        }
    }

    /**
     * Retrieves all students ordered by ID.
     */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, code, name, department FROM students ORDER BY id ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(new Student(
                        rs.getInt("id"),
                        rs.getString("code"),
                        rs.getString("name"),
                        rs.getString("department")
                ));
            }
        }
        return students;
    }

    /**
     * Retrieves a student by their unique student code (case-insensitive search).
     */
    public Student getStudentByCode(String code) throws SQLException {
        String sql = "SELECT id, code, name, department FROM students WHERE UPPER(code) = UPPER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("department")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a student by internal database ID.
     */
    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT id, code, name, department FROM students WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("id"),
                            rs.getString("code"),
                            rs.getString("name"),
                            rs.getString("department")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Updates an existing student record.
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET code = ?, name = ?, department = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getCode().trim());
            pstmt.setString(2, student.getName().trim());
            pstmt.setString(3, student.getDepartment().trim());
            pstmt.setInt(4, student.getId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a student by ID. Cascade deletion will remove associated attendance records.
     */
    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a student code already exists, optionally excluding a student ID (for updates).
     */
    public boolean studentCodeExists(String code, Integer excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) FROM students WHERE UPPER(code) = UPPER(?)"
                : "SELECT COUNT(*) FROM students WHERE UPPER(code) = UPPER(?) AND id != ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code.trim());
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}

package com.extremis.dao;

import com.extremis.database.DatabaseManager;
import com.extremis.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User authentication and single-session lock management.
 */
public class UserDao {
    private final DatabaseManager dbManager;

    public UserDao(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Authenticates credentials and role.
     */
    public User authenticate(String username, String password, String role) throws SQLException {
        String sql = "SELECT * FROM users WHERE UPPER(username) = UPPER(?) AND password = ? AND UPPER(role) = UPPER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            pstmt.setString(2, password.trim());
            pstmt.setString(3, role.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds user by username.
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE UPPER(username) = UPPER(?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds user by id.
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    /**
     * Gets all students with active sessions.
     */
    public List<User> getAllStudentsWithActiveSessions() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE active_session_id IS NOT NULL ORDER BY session_login_time DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        }
        return list;
    }

    /**
     * Updates user's active session.
     */
    public boolean createSession(int userId, String sessionId, String loginTime) throws SQLException {
        String sql = "UPDATE users SET active_session_id = ?, session_login_time = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sessionId);
            pstmt.setString(2, loginTime);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Clears active session upon logout.
     */
    public boolean clearSession(int userId) throws SQLException {
        String sql = "UPDATE users SET active_session_id = NULL, session_login_time = NULL WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a student account has an ongoing active session.
     */
    public boolean isStudentSessionActive(String studentCode) throws SQLException {
        String sql = "SELECT active_session_id FROM users WHERE UPPER(student_code) = UPPER(?) AND active_session_id IS NOT NULL";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentCode.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getString(1) != null && !rs.getString(1).isEmpty();
            }
        }
    }

    /**
     * Gets all currently logged-in users (active sessions).
     */
    public List<User> getActiveSessions() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE active_session_id IS NOT NULL ORDER BY session_login_time DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        }
        return list;
    }

    /**
     * Force logouts a user by administrator.
     */
    public boolean forceLogout(int userId) throws SQLException {
        return clearSession(userId);
    }

    /**
     * Adds a new user record.
     */
    public boolean addUser(User user) throws SQLException {
        String sql = """
            INSERT INTO users (username, password, role, full_name, department, student_code, active_session_id, session_login_time)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername().trim());
            pstmt.setString(2, user.getPassword().trim());
            pstmt.setString(3, user.getRole().toUpperCase().trim());
            pstmt.setString(4, user.getFullName().trim());
            pstmt.setString(5, user.getDepartment() != null ? user.getDepartment().trim() : null);
            pstmt.setString(6, user.getStudentCode() != null ? user.getStudentCode().trim() : null);
            pstmt.setString(7, user.getActiveSessionId());
            pstmt.setString(8, user.getSessionLoginTime());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieves all users.
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, username";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("full_name"),
                rs.getString("department"),
                rs.getString("student_code"),
                rs.getString("active_session_id"),
                rs.getString("session_login_time")
        );
    }
}

package com.extremis.service;

import com.extremis.dao.UserDao;
import com.extremis.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service managing user authentication, role separation, and single-session lock for students.
 */
public class AuthService {
    private final UserDao userDao;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Authenticates a user with role-specific verification and single-session lock for students.
     */
    public User login(String username, String password, String role) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password is required.");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new ValidationException("Please select a valid user role.");
        }

        User user = userDao.authenticate(username.trim(), password.trim(), role.trim());
        if (user == null) {
            throw new ValidationException("Authentication Failed: Invalid username or password for role " + role.toUpperCase() + ".");
        }

        // Single active session enforcement for students ONLY
        if (user.isStudent()) {
            if (user.getActiveSessionId() != null && !user.getActiveSessionId().trim().isEmpty()) {
                throw new ValidationException(String.format(
                        "🔒 Single Session Policy Enforced!\n\n" +
                        "Student '%s' (%s) already has an ACTIVE session running.\n" +
                        "Session ID: %s\n" +
                        "Logged in at: %s\n\n" +
                        "Multiple logins on another device or window are strictly blocked.\n" +
                        "Please log out from your active device or ask the Administrator to terminate your session.",
                        user.getFullName(), user.getUsername(), user.getActiveSessionId(), user.getSessionLoginTime()
                ));
            }

            // Create new single session
            String sessionId = "SES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String loginTime = LocalDateTime.now().format(TIME_FORMATTER);

            userDao.createSession(user.getId(), sessionId, loginTime);
            user.setActiveSessionId(sessionId);
            user.setSessionLoginTime(loginTime);
        }

        return user;
    }

    /**
     * Terminates an active session upon logout.
     */
    public void logout(User user) throws SQLException {
        if (user != null) {
            userDao.clearSession(user.getId());
            user.setActiveSessionId(null);
            user.setSessionLoginTime(null);
        }
    }

    /**
     * Force logouts a user by administrator.
     */
    public void forceLogout(int userId) throws SQLException {
        userDao.forceLogout(userId);
    }

    public UserDao getUserDao() {
        return userDao;
    }
}

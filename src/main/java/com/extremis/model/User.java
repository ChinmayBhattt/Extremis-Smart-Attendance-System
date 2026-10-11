package com.extremis.model;

/**
 * User model representing authenticated accounts (STUDENT, FACULTY, ADMIN).
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role; // "STUDENT", "FACULTY", "ADMIN"
    private String fullName;
    private String department;
    private String studentCode; // Linked student code if role is STUDENT
    private String activeSessionId; // Non-null if currently logged in
    private String sessionLoginTime;

    public User() {
    }

    public User(int id, String username, String password, String role, String fullName,
                String department, String studentCode, String activeSessionId, String sessionLoginTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.department = department;
        this.studentCode = studentCode;
        this.activeSessionId = activeSessionId;
        this.sessionLoginTime = sessionLoginTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getActiveSessionId() {
        return activeSessionId;
    }

    public void setActiveSessionId(String activeSessionId) {
        this.activeSessionId = activeSessionId;
    }

    public String getSessionLoginTime() {
        return sessionLoginTime;
    }

    public void setSessionLoginTime(String sessionLoginTime) {
        this.sessionLoginTime = sessionLoginTime;
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }

    public boolean isFaculty() {
        return "FACULTY".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}

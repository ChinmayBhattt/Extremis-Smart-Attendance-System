package com.extremis.model;

/**
 * Model representing a real-time lecture attendance record for a student.
 */
public class LectureAttendance {
    private int id;
    private int timetableId;
    private int studentId;
    private String studentCode;
    private String studentName;
    private String department;
    private String subject;
    private String date; // YYYY-MM-DD
    private String verifiedTime; // HH:mm:ss
    private String status; // "PRESENT", "ABSENT", "LATE"
    private boolean locationVerified;
    private boolean cameraVerified;

    public LectureAttendance() {
    }

    public LectureAttendance(int id, int timetableId, int studentId, String studentCode,
                             String studentName, String department, String subject,
                             String date, String verifiedTime, String status,
                             boolean locationVerified, boolean cameraVerified) {
        this.id = id;
        this.timetableId = timetableId;
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.department = department;
        this.subject = subject;
        this.date = date;
        this.verifiedTime = verifiedTime;
        this.status = status;
        this.locationVerified = locationVerified;
        this.cameraVerified = cameraVerified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(int timetableId) {
        this.timetableId = timetableId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVerifiedTime() {
        return verifiedTime;
    }

    public void setVerifiedTime(String verifiedTime) {
        this.verifiedTime = verifiedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isLocationVerified() {
        return locationVerified;
    }

    public void setLocationVerified(boolean locationVerified) {
        this.locationVerified = locationVerified;
    }

    public boolean isCameraVerified() {
        return cameraVerified;
    }

    public void setCameraVerified(boolean cameraVerified) {
        this.cameraVerified = cameraVerified;
    }
}

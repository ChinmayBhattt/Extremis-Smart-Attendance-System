package com.extremis.model;

import java.util.Objects;

/**
 * Represents an attendance record for a student on a specific date.
 */
public class Attendance {
    private int id;
    private int studentId;
    private String studentCode;
    private String studentName;
    private String department;
    private String date;       // ISO format: YYYY-MM-DD
    private String checkIn;    // e.g. HH:mm:ss
    private String checkOut;   // e.g. HH:mm:ss (or null if not checked out)

    public Attendance() {
    }

    public Attendance(int studentId, String date, String checkIn, String checkOut) {
        this.studentId = studentId;
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public Attendance(int id, int studentId, String date, String checkIn, String checkOut) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public boolean isCheckedOut() {
        return checkOut != null && !checkOut.trim().isEmpty();
    }

    public String getStatus() {
        return isCheckedOut() ? "Checked Out" : "Checked In (Open)";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance that = (Attendance) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", studentCode='" + studentCode + '\'' +
                ", studentName='" + studentName + '\'' +
                ", date='" + date + '\'' +
                ", checkIn='" + checkIn + '\'' +
                ", checkOut='" + checkOut + '\'' +
                '}';
    }
}

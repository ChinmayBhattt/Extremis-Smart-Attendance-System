package com.extremis.model;

/**
 * Model representing a scheduled lecture in the college timetable.
 */
public class TimeTableEntry {
    private int id;
    private String department;
    private String dayOfWeek; // e.g. "Monday", "Tuesday", etc.
    private String subject;
    private String facultyName;
    private String startTime; // "09:00"
    private String endTime;   // "10:00"
    private String verificationTime; // "09:05"
    private String roomNumber;

    public TimeTableEntry() {
    }

    public TimeTableEntry(int id, String department, String dayOfWeek, String subject,
                          String facultyName, String startTime, String endTime,
                          String verificationTime, String roomNumber) {
        this.id = id;
        this.department = department;
        this.dayOfWeek = dayOfWeek;
        this.subject = subject;
        this.facultyName = facultyName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.verificationTime = verificationTime;
        this.roomNumber = roomNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getVerificationTime() {
        return verificationTime;
    }

    public void setVerificationTime(String verificationTime) {
        this.verificationTime = verificationTime;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getTimeSlotDisplay() {
        return startTime + " - " + endTime;
    }
}

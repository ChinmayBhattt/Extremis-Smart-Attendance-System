package com.college.extremis.entity;

import com.college.extremis.entity.enums.AttendanceStatus;
import com.college.extremis.entity.enums.CheckoutType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_sessions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_slot_student_date", columnNames = {"timetable_slot_id", "student_id", "session_date"})
})
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "timetable_slot_id", nullable = false)
    private TimetableSlot timetableSlot;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AttendanceStatus status = AttendanceStatus.SCHEDULED;

    @Column(name = "confidence_score", nullable = false)
    private Integer confidenceScore = 100;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    @Column(name = "checkin_lat")
    private Double checkinLat;

    @Column(name = "checkin_lon")
    private Double checkinLon;

    @Column(name = "checkin_distance_meters")
    private Double checkinDistanceMeters;

    @Column(name = "checkin_accuracy_meters")
    private Double checkinAccuracyMeters;

    @Column(name = "checkin_photo_path")
    private String checkinPhotoPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkout_type", length = 30)
    private CheckoutType checkoutType;

    @Column(name = "flag_reason", length = 500)
    private String flagReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AttendanceSession() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TimetableSlot getTimetableSlot() { return timetableSlot; }
    public void setTimetableSlot(TimetableSlot timetableSlot) { this.timetableSlot = timetableSlot; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public Integer getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }

    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }

    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }

    public Double getCheckinLat() { return checkinLat; }
    public void setCheckinLat(Double checkinLat) { this.checkinLat = checkinLat; }

    public Double getCheckinLon() { return checkinLon; }
    public void setCheckinLon(Double checkinLon) { this.checkinLon = checkinLon; }

    public Double getCheckinDistanceMeters() { return checkinDistanceMeters; }
    public void setCheckinDistanceMeters(Double checkinDistanceMeters) { this.checkinDistanceMeters = checkinDistanceMeters; }

    public Double getCheckinAccuracyMeters() { return checkinAccuracyMeters; }
    public void setCheckinAccuracyMeters(Double checkinAccuracyMeters) { this.checkinAccuracyMeters = checkinAccuracyMeters; }

    public String getCheckinPhotoPath() { return checkinPhotoPath; }
    public void setCheckinPhotoPath(String checkinPhotoPath) { this.checkinPhotoPath = checkinPhotoPath; }

    public CheckoutType getCheckoutType() { return checkoutType; }
    public void setCheckoutType(CheckoutType checkoutType) { this.checkoutType = checkoutType; }

    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

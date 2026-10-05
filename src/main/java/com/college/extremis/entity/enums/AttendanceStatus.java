package com.college.extremis.entity.enums;

public enum AttendanceStatus {
    SCHEDULED,
    CHECKIN_PENDING,
    CHECKED_IN,
    UNDER_VERIFICATION,
    VERIFICATION_FAILED,
    CHECKED_OUT,
    COMPLETED,
    FLAGGED,
    APPROVED,
    CANCELLED;

    public String getBadgeClass() {
        return switch (this) {
            case SCHEDULED -> "badge bg-secondary";
            case CHECKIN_PENDING -> "badge bg-warning text-dark";
            case CHECKED_IN -> "badge bg-info text-dark";
            case UNDER_VERIFICATION -> "badge bg-primary";
            case VERIFICATION_FAILED, FLAGGED -> "badge bg-danger";
            case CHECKED_OUT -> "badge bg-light text-dark border";
            case COMPLETED, APPROVED -> "badge bg-success";
            case CANCELLED -> "badge bg-dark";
        };
    }
}

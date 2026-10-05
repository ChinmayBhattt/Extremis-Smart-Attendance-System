package com.college.extremis.entity.enums;

public enum Role {
    ROLE_STUDENT,
    ROLE_FACULTY,
    ROLE_CLASS_TEACHER,
    ROLE_ADMIN;

    public String getDisplayName() {
        return switch (this) {
            case ROLE_STUDENT -> "Student";
            case ROLE_FACULTY -> "Faculty";
            case ROLE_CLASS_TEACHER -> "Class Teacher";
            case ROLE_ADMIN -> "Administrator";
        };
    }
}

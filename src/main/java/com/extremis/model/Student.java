package com.extremis.model;

import java.util.Objects;

/**
 * Represents a student registered in the Extremis system.
 */
public class Student {
    private int id;
    private String code;
    private String name;
    private String department;
    private String email;
    private String section;

    public Student() {
    }

    public Student(String code, String name, String department) {
        this.code = code;
        this.name = name;
        this.department = department;
    }

    public Student(int id, String code, String name, String department) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.department = department;
    }

    public Student(int id, String code, String name, String department, String email, String section) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.department = department;
        this.email = email;
        this.section = section;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id && Objects.equals(code, student.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}

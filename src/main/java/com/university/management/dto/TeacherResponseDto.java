package com.university.management.dto;

public class TeacherResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String department;
    private String designation;

    public TeacherResponseDto() {
    }

    public TeacherResponseDto(Long id, String fullName, String email, String department, String designation) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.designation = designation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}


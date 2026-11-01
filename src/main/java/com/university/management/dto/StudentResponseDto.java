package com.university.management.dto;

public class StudentResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String department;
    private Integer semester;
    private String rollNumber;
    private boolean approved;

    public StudentResponseDto() {
    }

    public StudentResponseDto(Long id, String fullName, String email, String department, Integer semester, String rollNumber, boolean approved) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.semester = semester;
        this.rollNumber = rollNumber;
        this.approved = approved;
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

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}

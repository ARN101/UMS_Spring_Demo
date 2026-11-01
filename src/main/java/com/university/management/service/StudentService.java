package com.university.management.service;

import com.university.management.dto.StudentRequestDto;
import com.university.management.dto.StudentResponseDto;
import com.university.management.entity.Student;
import com.university.management.exception.DuplicateResourceException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.AdminRepository;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.TeacherRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          AdminRepository adminRepository,
                          TeacherRepository teacherRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<StudentResponseDto> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<StudentResponseDto> getApprovedStudents() {
        return studentRepository.findByApproved(true)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<StudentResponseDto> getPendingStudents() {
        return studentRepository.findByApproved(false)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public long getPendingCount() {
        return studentRepository.countByApproved(false);
    }

    public StudentResponseDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return mapToResponseDto(student);
    }

    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));
    }

    public StudentResponseDto createStudent(StudentRequestDto dto, boolean approved) {
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty when creating an account");
        }

        validateEmailUniqueness(dto.getEmail(), null);
        validateRollNumberUniqueness(dto.getRollNumber(), null);

        Student student = new Student();
        student.setFullName(dto.getFullName());
        student.setEmail(dto.getEmail());
        student.setPassword(passwordEncoder.encode(dto.getPassword()));
        student.setDepartment(dto.getDepartment());
        student.setSemester(dto.getSemester());
        student.setRollNumber(dto.getRollNumber());
        student.setApproved(approved);

        Student saved = studentRepository.save(student);
        return mapToResponseDto(saved);
    }

    public StudentResponseDto approveStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        student.setApproved(true);
        Student updated = studentRepository.save(student);
        return mapToResponseDto(updated);
    }

    public StudentResponseDto updateStudent(Long id, StudentRequestDto dto, String authenticatedEmail, boolean isAdmin) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (!isAdmin && (authenticatedEmail == null || !student.getEmail().equalsIgnoreCase(authenticatedEmail))) {
            throw new AccessDeniedException("You are only allowed to edit your own student profile");
        }

        validateEmailUniqueness(dto.getEmail(), id);
        validateRollNumberUniqueness(dto.getRollNumber(), id);

        student.setFullName(dto.getFullName());
        student.setEmail(dto.getEmail());
        student.setDepartment(dto.getDepartment());
        student.setSemester(dto.getSemester());
        student.setRollNumber(dto.getRollNumber());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            student.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Student updated = studentRepository.save(student);
        return mapToResponseDto(updated);
    }

    public void deleteStudent(Long id, String authenticatedEmail, boolean isAdmin) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (!isAdmin && (authenticatedEmail == null || !student.getEmail().equalsIgnoreCase(authenticatedEmail))) {
            throw new AccessDeniedException("You are only allowed to delete your own student profile");
        }

        studentRepository.deleteById(id);
    }

    public void validateEmailUniqueness(String email, Long currentStudentId) {
        Optional<Student> existingStudent = studentRepository.findByEmail(email);
        if (existingStudent.isPresent() && (currentStudentId == null || !existingStudent.get().getId().equals(currentStudentId))) {
            throw new DuplicateResourceException("Email is already in use: " + email);
        }

        if (adminRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use by an admin: " + email);
        }

        if (teacherRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use by a teacher: " + email);
        }
    }

    public void validateRollNumberUniqueness(String rollNumber, Long currentStudentId) {
        Optional<Student> existingStudent = studentRepository.findByRollNumber(rollNumber);
        if (existingStudent.isPresent() && (currentStudentId == null || !existingStudent.get().getId().equals(currentStudentId))) {
            throw new DuplicateResourceException("Roll number is already in use: " + rollNumber);
        }
    }

    public StudentResponseDto mapToResponseDto(Student student) {
        return new StudentResponseDto(
                student.getId(),
                student.getFullName(),
                student.getEmail(),
                student.getDepartment(),
                student.getSemester(),
                student.getRollNumber(),
                student.isApproved()
        );
    }
}

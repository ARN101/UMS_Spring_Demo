package com.university.management.service;

import com.university.management.dto.TeacherRequestDto;
import com.university.management.dto.TeacherResponseDto;
import com.university.management.entity.Teacher;
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
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository,
                          AdminRepository adminRepository,
                          StudentRepository studentRepository,
                          PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<TeacherResponseDto> getAllTeachers() {
        return teacherRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public TeacherResponseDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return mapToResponseDto(teacher);
    }

    public Teacher getTeacherByEmail(String email) {
        return teacherRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with email: " + email));
    }

    public TeacherResponseDto createTeacher(TeacherRequestDto dto) {
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty when creating an account");
        }

        validateEmailUniqueness(dto.getEmail(), null);

        Teacher teacher = new Teacher();
        teacher.setFullName(dto.getFullName());
        teacher.setEmail(dto.getEmail());
        teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        teacher.setDepartment(dto.getDepartment());
        teacher.setDesignation(dto.getDesignation());

        Teacher saved = teacherRepository.save(teacher);
        return mapToResponseDto(saved);
    }

    public TeacherResponseDto updateTeacher(Long id, TeacherRequestDto dto, String authenticatedEmail, boolean isAdmin) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        if (!isAdmin && (authenticatedEmail == null || !teacher.getEmail().equalsIgnoreCase(authenticatedEmail))) {
            throw new AccessDeniedException("You are only allowed to edit your own teacher profile");
        }

        validateEmailUniqueness(dto.getEmail(), id);

        teacher.setFullName(dto.getFullName());
        teacher.setEmail(dto.getEmail());
        teacher.setDepartment(dto.getDepartment());
        teacher.setDesignation(dto.getDesignation());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            teacher.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Teacher updated = teacherRepository.save(teacher);
        return mapToResponseDto(updated);
    }

    public void deleteTeacher(Long id, String authenticatedEmail, boolean isAdmin) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        if (!isAdmin && (authenticatedEmail == null || !teacher.getEmail().equalsIgnoreCase(authenticatedEmail))) {
            throw new AccessDeniedException("You are only allowed to delete your own teacher profile");
        }

        teacherRepository.deleteById(id);
    }

    public void validateEmailUniqueness(String email, Long currentTeacherId) {
        Optional<Teacher> existingTeacher = teacherRepository.findByEmail(email);
        if (existingTeacher.isPresent() && (currentTeacherId == null || !existingTeacher.get().getId().equals(currentTeacherId))) {
            throw new DuplicateResourceException("Email is already in use: " + email);
        }

        if (adminRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use by an admin: " + email);
        }

        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use by a student: " + email);
        }
    }

    public TeacherResponseDto mapToResponseDto(Teacher teacher) {
        return new TeacherResponseDto(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getEmail(),
                teacher.getDepartment(),
                teacher.getDesignation()
        );
    }
}

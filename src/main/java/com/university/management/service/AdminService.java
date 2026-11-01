package com.university.management.service;

import com.university.management.dto.AdminRequestDto;
import com.university.management.dto.AdminResponseDto;
import com.university.management.entity.Admin;
import com.university.management.exception.DuplicateResourceException;
import com.university.management.exception.ResourceNotFoundException;
import com.university.management.repository.AdminRepository;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.TeacherRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository,
                        TeacherRepository teacherRepository,
                        StudentRepository studentRepository,
                        PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminResponseDto> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public AdminResponseDto getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return mapToResponseDto(admin);
    }

    public Admin getAdminEntityById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
    }

    public AdminResponseDto createAdmin(AdminRequestDto dto) {
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty when creating an account");
        }

        validateEmailUniqueness(dto.getEmail(), null);

        Admin admin = new Admin();
        admin.setFullName(dto.getFullName());
        admin.setEmail(dto.getEmail());
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));

        Admin saved = adminRepository.save(admin);
        return mapToResponseDto(saved);
    }

    public AdminResponseDto updateAdmin(Long id, AdminRequestDto dto) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        validateEmailUniqueness(dto.getEmail(), id);

        admin.setFullName(dto.getFullName());
        admin.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        Admin updated = adminRepository.save(admin);
        return mapToResponseDto(updated);
    }

    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new ResourceNotFoundException("Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
    }

    public void validateEmailUniqueness(String email, Long currentAdminId) {
        Optional<Admin> existingAdmin = adminRepository.findByEmail(email);
        if (existingAdmin.isPresent() && (currentAdminId == null || !existingAdmin.get().getId().equals(currentAdminId))) {
            throw new DuplicateResourceException("Email is already in use: " + email);
        }

        if (teacherRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use by a teacher: " + email);
        }

        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already in use by a student: " + email);
        }
    }

    public AdminResponseDto mapToResponseDto(Admin admin) {
        return new AdminResponseDto(
                admin.getId(),
                admin.getFullName(),
                admin.getEmail()
        );
    }
}


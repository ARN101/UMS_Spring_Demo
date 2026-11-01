package com.university.management.api;

import com.university.management.dto.StudentRequestDto;
import com.university.management.dto.StudentResponseDto;
import com.university.management.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentRestController {

    private final StudentService studentService;

    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto dto, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        if (!isAdmin && !isTeacher) {
            throw new AccessDeniedException("Students cannot create other student accounts");
        }

        // Admin creation is auto-approved, teacher creation requires admin approval
        boolean approved = isAdmin;
        StudentResponseDto created = studentService.createStudent(dto, approved);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<StudentResponseDto> approveStudent(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can approve student accounts");
        }
        StudentResponseDto approved = studentService.approveStudent(id);
        return ResponseEntity.ok(approved);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> getAllStudents(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<StudentResponseDto> students = isAdmin ? studentService.getAllStudents() : studentService.getApprovedStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getStudentById(@PathVariable Long id) {
        StudentResponseDto student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> updateStudent(@PathVariable Long id,
                                                            @Valid @RequestBody StudentRequestDto dto,
                                                            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStudent = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        if (!isAdmin && !isStudent) {
            throw new AccessDeniedException("Teachers cannot modify student accounts");
        }

        StudentResponseDto updated = studentService.updateStudent(id, dto, authentication.getName(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStudent = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        if (!isAdmin && !isStudent) {
            throw new AccessDeniedException("Teachers cannot delete student accounts");
        }

        studentService.deleteStudent(id, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}

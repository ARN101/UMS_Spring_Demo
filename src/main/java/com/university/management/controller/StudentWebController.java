package com.university.management.controller;

import com.university.management.dto.StudentRequestDto;
import com.university.management.dto.StudentResponseDto;
import com.university.management.exception.DuplicateResourceException;
import com.university.management.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student/students")
public class StudentWebController {

    private final StudentService studentService;

    public StudentWebController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String listStudents(Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));
        boolean isStudent = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        List<StudentResponseDto> students = isAdmin ? studentService.getAllStudents() : studentService.getApprovedStudents();

        model.addAttribute("students", students);
        model.addAttribute("currentUserEmail", authentication.getName());
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isTeacher", isTeacher);
        model.addAttribute("isStudent", isStudent);
        return "student/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        if (!isAdmin && !isTeacher) {
            throw new AccessDeniedException("Students cannot register other students");
        }

        model.addAttribute("student", new StudentRequestDto());
        model.addAttribute("isTeacher", isTeacher);
        return "student/form";
    }

    @PostMapping
    public String createStudent(@Valid @ModelAttribute("student") StudentRequestDto dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isTeacher = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        if (!isAdmin && !isTeacher) {
            throw new AccessDeniedException("Students cannot register other students");
        }

        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.password", "Password cannot be empty");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("isTeacher", isTeacher);
            return "student/form";
        }

        try {
            boolean autoApproved = isAdmin;
            studentService.createStudent(dto, autoApproved);

            if (autoApproved) {
                redirectAttributes.addFlashAttribute("successMessage", "Student created successfully");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Student registered successfully! Pending administrator approval before they can log in.");
            }

            return "redirect:/student/students";
        } catch (DuplicateResourceException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isTeacher", isTeacher);
            return "student/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        StudentResponseDto student = studentService.getStudentById(id);

        if (!isAdmin && !student.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("You are only allowed to edit your own student profile");
        }

        StudentRequestDto dto = new StudentRequestDto(
                student.getFullName(),
                student.getEmail(),
                "",
                student.getDepartment(),
                student.getSemester(),
                student.getRollNumber()
        );
        model.addAttribute("student", dto);
        model.addAttribute("studentId", id);
        return "student/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateStudent(@PathVariable Long id,
                               @Valid @ModelAttribute("student") StudentRequestDto dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (bindingResult.hasFieldErrors("fullName") || bindingResult.hasFieldErrors("email")
                || bindingResult.hasFieldErrors("department") || bindingResult.hasFieldErrors("semester")
                || bindingResult.hasFieldErrors("rollNumber")) {
            model.addAttribute("studentId", id);
            return "student/edit";
        }

        try {
            studentService.updateStudent(id, dto, authentication.getName(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Student profile updated successfully");
            return "redirect:/student/students";
        } catch (DuplicateResourceException | IllegalArgumentException | AccessDeniedException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("studentId", id);
            return "student/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id,
                                Authentication authentication,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            StudentResponseDto student = studentService.getStudentById(id);
            boolean isSelf = student.getEmail().equalsIgnoreCase(authentication.getName());

            studentService.deleteStudent(id, authentication.getName(), isAdmin);

            if (isSelf && !isAdmin) {
                request.getSession().invalidate();
                return "redirect:/login?deleted=true";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/students";
    }
}

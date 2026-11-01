package com.university.management.controller;

import com.university.management.dto.TeacherRequestDto;
import com.university.management.dto.TeacherResponseDto;
import com.university.management.exception.DuplicateResourceException;
import com.university.management.service.TeacherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/teacher/teachers")
public class TeacherWebController {

    private final TeacherService teacherService;

    public TeacherWebController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String listTeachers(Model model, Authentication authentication) {
        model.addAttribute("teachers", teacherService.getAllTeachers());
        model.addAttribute("currentUserEmail", authentication.getName());
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);
        return "teacher/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can add teacher records");
        }
        model.addAttribute("teacher", new TeacherRequestDto());
        return "teacher/form";
    }

    @PostMapping
    public String createTeacher(@Valid @ModelAttribute("teacher") TeacherRequestDto dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can add teacher records");
        }

        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.password", "Password cannot be empty");
        }

        if (bindingResult.hasErrors()) {
            return "teacher/form";
        }

        try {
            teacherService.createTeacher(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher created successfully");
            return "redirect:/teacher/teachers";
        } catch (DuplicateResourceException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "teacher/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        TeacherResponseDto teacher = teacherService.getTeacherById(id);

        if (!isAdmin && !teacher.getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("You can only edit your own teacher profile");
        }

        TeacherRequestDto dto = new TeacherRequestDto(
                teacher.getFullName(),
                teacher.getEmail(),
                "",
                teacher.getDepartment(),
                teacher.getDesignation()
        );
        model.addAttribute("teacher", dto);
        model.addAttribute("teacherId", id);
        return "teacher/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTeacher(@PathVariable Long id,
                               @Valid @ModelAttribute("teacher") TeacherRequestDto dto,
                               BindingResult bindingResult,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (bindingResult.hasFieldErrors("fullName") || bindingResult.hasFieldErrors("email")
                || bindingResult.hasFieldErrors("department") || bindingResult.hasFieldErrors("designation")) {
            model.addAttribute("teacherId", id);
            return "teacher/edit";
        }

        try {
            teacherService.updateTeacher(id, dto, authentication.getName(), isAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher profile updated successfully");
            return "redirect:/teacher/teachers";
        } catch (DuplicateResourceException | IllegalArgumentException | AccessDeniedException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("teacherId", id);
            return "teacher/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTeacher(@PathVariable Long id,
                                Authentication authentication,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        try {
            TeacherResponseDto teacher = teacherService.getTeacherById(id);
            boolean isSelf = teacher.getEmail().equalsIgnoreCase(authentication.getName());

            teacherService.deleteTeacher(id, authentication.getName(), isAdmin);

            if (isSelf && !isAdmin) {
                request.getSession().invalidate();
                return "redirect:/login?deleted=true";
            }

            redirectAttributes.addFlashAttribute("successMessage", "Teacher deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/teacher/teachers";
    }
}

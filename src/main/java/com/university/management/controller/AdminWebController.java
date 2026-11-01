package com.university.management.controller;

import com.university.management.dto.AdminRequestDto;
import com.university.management.dto.AdminResponseDto;
import com.university.management.exception.DuplicateResourceException;
import com.university.management.service.AdminService;
import com.university.management.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final AdminService adminService;
    private final StudentService studentService;

    public AdminWebController(AdminService adminService, StudentService studentService) {
        this.adminService = adminService;
        this.studentService = studentService;
    }

    @GetMapping("/admins")
    public String listAdmins(Model model) {
        model.addAttribute("admins", adminService.getAllAdmins());
        return "admin/list";
    }

    @GetMapping("/admins/new")
    public String showCreateForm(Model model) {
        model.addAttribute("admin", new AdminRequestDto());
        return "admin/form";
    }

    @PostMapping("/admins")
    public String createAdmin(@Valid @ModelAttribute("admin") AdminRequestDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.password", "Password cannot be empty");
        }

        if (bindingResult.hasErrors()) {
            return "admin/form";
        }

        try {
            adminService.createAdmin(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Admin created successfully");
            return "redirect:/admin/admins";
        } catch (DuplicateResourceException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/form";
        }
    }

    @GetMapping("/admins/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        AdminResponseDto admin = adminService.getAdminById(id);
        AdminRequestDto dto = new AdminRequestDto(admin.getFullName(), admin.getEmail(), "");
        model.addAttribute("admin", dto);
        model.addAttribute("adminId", id);
        return "admin/edit";
    }

    @PostMapping("/admins/edit/{id}")
    public String updateAdmin(@PathVariable Long id,
                              @Valid @ModelAttribute("admin") AdminRequestDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasFieldErrors("fullName") || bindingResult.hasFieldErrors("email")) {
            model.addAttribute("adminId", id);
            return "admin/edit";
        }

        try {
            adminService.updateAdmin(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Admin updated successfully");
            return "redirect:/admin/admins";
        } catch (DuplicateResourceException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("adminId", id);
            return "admin/edit";
        }
    }

    @PostMapping("/admins/delete/{id}")
    public String deleteAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteAdmin(id);
            redirectAttributes.addFlashAttribute("successMessage", "Admin deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/admins";
    }

    @GetMapping("/students/pending")
    public String listPendingStudents(Model model) {
        model.addAttribute("pendingStudents", studentService.getPendingStudents());
        return "admin/pending-students";
    }

    @PostMapping("/students/approve/{id}")
    public String approveStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.approveStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student approved successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/students/pending";
    }

    @PostMapping("/students/reject/{id}")
    public String rejectStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id, null, true);
            redirectAttributes.addFlashAttribute("successMessage", "Pending student request rejected and deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/students/pending";
    }
}

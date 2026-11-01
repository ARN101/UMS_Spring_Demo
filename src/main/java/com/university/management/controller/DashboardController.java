package com.university.management.controller;

import com.university.management.entity.Student;
import com.university.management.entity.Teacher;
import com.university.management.service.StudentService;
import com.university.management.service.TeacherService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StudentService studentService;
    private final TeacherService teacherService;

    public DashboardController(StudentService studentService, TeacherService teacherService) {
        this.studentService = studentService;
        this.teacherService = teacherService;
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        model.addAttribute("userEmail", authentication.getName());
        model.addAttribute("role", "ADMIN");
        model.addAttribute("pendingCount", studentService.getPendingCount());
        return "admin/dashboard";
    }

    @GetMapping("/teacher/dashboard")
    public String teacherDashboard(Authentication authentication, Model model) {
        model.addAttribute("userEmail", authentication.getName());
        model.addAttribute("role", "TEACHER");
        try {
            Teacher teacher = teacherService.getTeacherByEmail(authentication.getName());
            model.addAttribute("myTeacherId", teacher.getId());
            model.addAttribute("teacher", teacher);
        } catch (Exception ignored) {
        }
        return "teacher/dashboard";
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        model.addAttribute("userEmail", authentication.getName());
        model.addAttribute("role", "STUDENT");
        try {
            Student student = studentService.getStudentByEmail(authentication.getName());
            model.addAttribute("myStudentId", student.getId());
            model.addAttribute("student", student);
        } catch (Exception ignored) {
        }
        return "student/dashboard";
    }
}

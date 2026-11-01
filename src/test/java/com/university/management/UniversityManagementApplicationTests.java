package com.university.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.management.dto.AdminRequestDto;
import com.university.management.dto.StudentRequestDto;
import com.university.management.dto.TeacherRequestDto;
import com.university.management.entity.Admin;
import com.university.management.entity.Student;
import com.university.management.entity.Teacher;
import com.university.management.repository.AdminRepository;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UniversityManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        teacherRepository.deleteAll();
        adminRepository.deleteAll();

        // Re-seed initial admin
        Admin admin = new Admin();
        admin.setFullName("Kazi Farhan Ahmed");
        admin.setEmail("admin@university.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        adminRepository.save(admin);
    }

    @Test
    void contextLoadsAndDefaultAdminSeeded() {
        assertTrue(adminRepository.existsByEmail("admin@university.com"));
        Admin admin = adminRepository.findByEmail("admin@university.com").orElseThrow();
        assertTrue(passwordEncoder.matches("admin123", admin.getPassword()));
    }

    // --- Authentication & Security Tests ---

    @Test
    void unauthenticatedApiAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @WithMockUser(username = "rafiqul.islam@university.com", roles = {"TEACHER"})
    void teacherAccessingAdminApiReturns403() throws Exception {
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @WithMockUser(username = "shakil.ahmed@university.com", roles = {"STUDENT"})
    void studentAccessingTeacherApiReturns403() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    // --- Admin REST API Tests ---

    @Test
    @WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
    void adminCanCreateAndGetAdmins() throws Exception {
        AdminRequestDto newAdmin = new AdminRequestDto("Mohammad Ashraful Alam", "ashraful@university.com", "password123");

        mockMvc.perform(post("/api/admins")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAdmin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Mohammad Ashraful Alam"))
                .andExpect(jsonPath("$.email").value("ashraful@university.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
    void duplicateEmailReturns409() throws Exception {
        AdminRequestDto duplicate = new AdminRequestDto("Another Admin", "admin@university.com", "password123");

        mockMvc.perform(post("/api/admins")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // --- Teacher Tests: View all, edit self only, admin creates ---

    @Test
    @WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
    void adminCanCreateTeacher() throws Exception {
        TeacherRequestDto newTeacher = new TeacherRequestDto(
                "Dr. Mohammad Rafiqul Islam",
                "rafiqul.islam@university.com",
                "password123",
                "Computer Science and Engineering",
                "Professor"
        );

        mockMvc.perform(post("/api/teachers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTeacher)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Dr. Mohammad Rafiqul Islam"))
                .andExpect(jsonPath("$.email").value("rafiqul.islam@university.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.department").value("Computer Science and Engineering"))
                .andExpect(jsonPath("$.designation").value("Professor"));
    }

    @Test
    @WithMockUser(username = "rafiqul.islam@university.com", roles = {"TEACHER"})
    void teacherCanEditSelfOnly() throws Exception {
        Teacher t1 = new Teacher("Dr. Mohammad Rafiqul Islam", "rafiqul.islam@university.com", passwordEncoder.encode("pass123"), "CSE", "Professor");
        Teacher t2 = new Teacher("Nusrat Jahan Chowdhury", "nusrat.jahan@university.com", passwordEncoder.encode("pass123"), "EEE", "Associate Professor");
        teacherRepository.save(t1);
        teacherRepository.save(t2);

        // Edit self -> Allowed
        TeacherRequestDto updateSelf = new TeacherRequestDto("Dr. Mohammad Rafiqul Islam Updated", "rafiqul.islam@university.com", "newpass123", "CSE", "Professor");
        mockMvc.perform(put("/api/teachers/" + t1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSelf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Dr. Mohammad Rafiqul Islam Updated"));

        // Edit other teacher -> Forbidden (403)
        TeacherRequestDto updateOther = new TeacherRequestDto("Hacked", "nusrat.jahan@university.com", "newpass123", "EEE", "Associate Professor");
        mockMvc.perform(put("/api/teachers/" + t2.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOther)))
                .andExpect(status().isForbidden());
    }

    // --- Student Tests: Teacher registers student pending approval, Admin approves ---

    @Test
    @WithMockUser(username = "rafiqul.islam@university.com", roles = {"TEACHER"})
    void teacherRegistersStudentPendingApproval() throws Exception {
        StudentRequestDto studentDto = new StudentRequestDto(
                "Abrar Fahim Hossain",
                "abrar.fahim@university.com",
                "password123",
                "Electrical and Electronic Engineering",
                1,
                "EEE-2024-0312"
        );

        mockMvc.perform(post("/api/students")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.approved").value(false));

        Student saved = studentRepository.findByEmail("abrar.fahim@university.com").orElseThrow();
        assertFalse(saved.isApproved());
    }

    @Test
    @WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
    void adminCanApproveStudent() throws Exception {
        Student pending = new Student("Abrar Fahim Hossain", "abrar.fahim@university.com", passwordEncoder.encode("pass123"), "EEE", 1, "EEE-2024-0312", false);
        studentRepository.save(pending);

        mockMvc.perform(put("/api/students/" + pending.getId() + "/approve")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approved").value(true));

        Student approved = studentRepository.findById(pending.getId()).orElseThrow();
        assertTrue(approved.isApproved());
    }

    @Test
    @WithMockUser(username = "shakil.ahmed@university.com", roles = {"STUDENT"})
    void studentCanEditSelfOnly() throws Exception {
        Student s1 = new Student("Md. Shakil Ahmed", "shakil.ahmed@university.com", passwordEncoder.encode("pass123"), "CSE", 4, "CSE-2023-0101", true);
        Student s2 = new Student("Farzana Rahman Mim", "farzana.mim@university.com", passwordEncoder.encode("pass123"), "SWE", 3, "SWE-2023-0205", true);
        studentRepository.save(s1);
        studentRepository.save(s2);

        // Edit self -> Allowed
        StudentRequestDto updateSelf = new StudentRequestDto("Md. Shakil Ahmed Updated", "shakil.ahmed@university.com", "pass123", "CSE", 4, "CSE-2023-0101");
        mockMvc.perform(put("/api/students/" + s1.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSelf)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Md. Shakil Ahmed Updated"));

        // Edit other student -> Forbidden (403)
        StudentRequestDto updateOther = new StudentRequestDto("Farzana Hacked", "farzana.mim@university.com", "pass123", "SWE", 3, "SWE-2023-0205");
        mockMvc.perform(put("/api/students/" + s2.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOther)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
    void notFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/admins/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "admin@university.com", roles = {"ADMIN"})
    void invalidInputReturns400() throws Exception {
        AdminRequestDto invalid = new AdminRequestDto("", "not-an-email", "");

        mockMvc.perform(post("/api/admins")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}

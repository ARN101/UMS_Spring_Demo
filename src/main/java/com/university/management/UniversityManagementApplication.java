package com.university.management;

import com.university.management.entity.Admin;
import com.university.management.entity.Student;
import com.university.management.entity.Teacher;
import com.university.management.repository.AdminRepository;
import com.university.management.repository.StudentRepository;
import com.university.management.repository.TeacherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class UniversityManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(UniversityManagementApplication.class, args);
    }

    @Bean
    public CommandLineRunner initSeedData(AdminRepository adminRepository,
                                          TeacherRepository teacherRepository,
                                          StudentRepository studentRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Initial Admin
            String defaultAdminEmail = "admin@university.com";
            if (!adminRepository.existsByEmail(defaultAdminEmail)) {
                Admin admin = new Admin();
                admin.setFullName("Kazi Farhan Ahmed");
                admin.setEmail(defaultAdminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                adminRepository.save(admin);
            }

            // 2. Initial Bangladeshi Teachers
            if (!teacherRepository.existsByEmail("rafiqul.islam@university.com")) {
                Teacher t1 = new Teacher(
                        "Dr. Mohammad Rafiqul Islam",
                        "rafiqul.islam@university.com",
                        passwordEncoder.encode("password123"),
                        "Computer Science and Engineering",
                        "Professor & Head of Department"
                );
                teacherRepository.save(t1);
            }

            if (!teacherRepository.existsByEmail("nusrat.jahan@university.com")) {
                Teacher t2 = new Teacher(
                        "Nusrat Jahan Chowdhury",
                        "nusrat.jahan@university.com",
                        passwordEncoder.encode("password123"),
                        "Electrical and Electronic Engineering",
                        "Associate Professor"
                );
                teacherRepository.save(t2);
            }

            // 3. Initial Bangladeshi Students (Approved)
            if (!studentRepository.existsByEmail("shakil.ahmed@university.com")) {
                Student s1 = new Student(
                        "Md. Shakil Ahmed",
                        "shakil.ahmed@university.com",
                        passwordEncoder.encode("password123"),
                        "Computer Science and Engineering",
                        4,
                        "CSE-2023-0101",
                        true
                );
                studentRepository.save(s1);
            }

            if (!studentRepository.existsByEmail("farzana.mim@university.com")) {
                Student s2 = new Student(
                        "Farzana Rahman Mim",
                        "farzana.mim@university.com",
                        passwordEncoder.encode("password123"),
                        "Software Engineering",
                        3,
                        "SWE-2023-0205",
                        true
                );
                studentRepository.save(s2);
            }

            // 4. Seeded Pending Student (Requires Admin approval)
            if (!studentRepository.existsByEmail("abrar.fahim@university.com")) {
                Student s3 = new Student(
                        "Abrar Fahim Hossain",
                        "abrar.fahim@university.com",
                        passwordEncoder.encode("password123"),
                        "Electrical and Electronic Engineering",
                        1,
                        "EEE-2024-0312",
                        false
                );
                studentRepository.save(s3);
            }
        };
    }
}

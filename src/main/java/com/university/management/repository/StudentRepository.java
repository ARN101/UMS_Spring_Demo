package com.university.management.repository;

import com.university.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRollNumber(String rollNumber);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByApproved(boolean approved);
    long countByApproved(boolean approved);
}

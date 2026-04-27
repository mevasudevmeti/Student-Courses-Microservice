package com.lbu.student_service.repository;

import com.lbu.student_service.entities.Enrollment;
import com.lbu.student_service.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    // Used to check if it's the student's first time enrolling
    long countByStudent(Student student);
}
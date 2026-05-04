package com.lbu.student_service.repository;

import com.lbu.student_service.entities.Course;
import com.lbu.student_service.entities.Enrollment;
import com.lbu.student_service.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Derived query methods such as findByStudentAndCourse are translated by Spring Data JPA into SQL.
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);

    List<Enrollment> findByStudentStudentId(Long studentId);
}

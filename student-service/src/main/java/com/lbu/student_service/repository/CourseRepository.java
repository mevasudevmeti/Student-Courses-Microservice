package com.lbu.student_service.repository;

import com.lbu.student_service.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA creates the database access implementation at runtime.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {
}

package com.lbu.student_service.repository;

import com.lbu.student_service.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
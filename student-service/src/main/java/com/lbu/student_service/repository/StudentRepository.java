package com.lbu.student_service.repository;

import com.lbu.student_service.entities.Student;
import com.lbu.student_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository methods provide CRUD and query operations for Student rows without boilerplate SQL.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUser(User user);

    Optional<Student> findByEmail(String email);
}

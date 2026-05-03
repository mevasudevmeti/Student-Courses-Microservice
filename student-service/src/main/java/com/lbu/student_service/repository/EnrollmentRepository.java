package com.lbu.student_service.repository;

import com.lbu.student_service.entities.Enrollment;
import com.lbu.student_service.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    // used to check if it's the student's first time enrolling
    long countByStudent(Student student);

    @Query("SELECT e FROM Enrollment e WHERE e.student.studentId = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);
}
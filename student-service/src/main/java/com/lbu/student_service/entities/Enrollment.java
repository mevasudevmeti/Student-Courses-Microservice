package com.lbu.student_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * This entity models the many-to-one relationship from enrolments to students and courses.
 * The unique constraint prevents a student from enrolling in the same course twice.
 *
 * invoiceReference stores the Finance microservice result without copying Finance's full data model.
 */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_course", columnNames = {"student_id", "course_id"})
})
@Data
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "invoice_reference")
    private String invoiceReference;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate = LocalDate.now();

    public Enrollment(Student student, Course course, String invoiceReference) {
        this.student = student;
        this.course = course;
        this.invoiceReference = invoiceReference;
        this.enrollmentDate = LocalDate.now();
    }
}

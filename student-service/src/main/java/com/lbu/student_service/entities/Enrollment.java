package com.lbu.student_service.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Column(name = "invoice_reference")
    private String invoiceReference;

    // FIX: Add this field if it's missing, and initialize it to LocalDate.now()
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate = LocalDate.now();

    public Enrollment(Student student, Course course, String invoiceReference) {
        this.student = student;
        this.course = course;
        this.invoiceReference = invoiceReference;
        this.enrollmentDate = LocalDate.now(); // Set date on creation
    }
}
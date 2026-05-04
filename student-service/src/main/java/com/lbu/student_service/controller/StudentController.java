package com.lbu.student_service.controller;

import com.lbu.student_service.dto.AuthResponse;
import com.lbu.student_service.dto.InvoiceDto;
import com.lbu.student_service.entities.Course;
import com.lbu.student_service.entities.Enrollment;
import com.lbu.student_service.entities.Student;
import com.lbu.student_service.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * This controller exposes the Student portal REST API for registration, login, courses,
 * enrolments, profile updates and graduation checks.
 *
 * The controller receives StudentService through constructor injection and delegates business rules
 * to the service layer.
 */
@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8083"})
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/register")
    public ResponseEntity<Student> register(@RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.register(student));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        AuthResponse auth = studentService.login(credentials.get("username"), credentials.get("password"));
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }
        return ResponseEntity.ok(auth);
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(studentService.getAllCourses());
    }

    @PostMapping("/enrol")
    public ResponseEntity<Map<String, Object>> enrol(@RequestParam Long studentId, @RequestParam Long courseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.enrolInCourse(studentId, courseId));
    }

    @GetMapping("/{studentId}/graduation-eligibility")
    public ResponseEntity<Map<String, Object>> checkGraduation(@PathVariable Long studentId) {
        boolean eligible = studentService.isEligibleToGraduate(studentId);
        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "eligible", eligible,
                "message", eligible
                        ? "Eligible to graduate: no outstanding invoices found."
                        : "Not eligible to graduate: outstanding balance exists or Finance service is unavailable."
        ));
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<Student> getProfile(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getStudentById(studentId));
    }

    @PatchMapping("/{studentId}")
    public ResponseEntity<Student> updateProfile(@PathVariable Long studentId,
                                                 @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(studentService.updateStudent(studentId, updates));
    }

    @GetMapping("/{studentId}/enrolments")
    public ResponseEntity<List<Enrollment>> getMyEnrolments(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getStudentEnrolments(studentId));
    }

    @PostMapping("/{studentId}/library-fine")
    public ResponseEntity<InvoiceDto> createLibraryFine(@PathVariable Long studentId,
                                                        @RequestParam Double amount,
                                                        @RequestParam(required = false) String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.createLibraryFineInvoice(studentId, amount, description));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "student-service"));
    }
}

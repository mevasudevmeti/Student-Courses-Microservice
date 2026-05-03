package com.lbu.student_service.controller;

import com.lbu.student_service.clients.FinanceClient;
import com.lbu.student_service.dto.InvoiceDto;
import com.lbu.student_service.entities.Enrollment;
import com.lbu.student_service.entities.Student;
import com.lbu.student_service.entities.Course;
import com.lbu.student_service.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // 1. REGISTER: Creates user, student, and external accounts
    @PostMapping("/register")
    public ResponseEntity<Student> register(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.register(student));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Student student = studentService.login(username, password);

        if (student != null) {
            return ResponseEntity.ok(student);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    // 2. VIEW COURSES: Requirement "View all the courses offered"
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(studentService.getAllCourses());
    }

    // 3. ENROL: Requirement "Enrol in course"
    // Expects JSON like: {"studentId": 1, "courseId": 101}
    @PostMapping("/enrol")
    public ResponseEntity<Map<String, Object>> enrol(@RequestParam Long studentId, @RequestParam Long courseId) {
        return ResponseEntity.ok(studentService.enrolInCourse(studentId, courseId));
    }

    // 4. GRADUATION: Requirement "view eligibility to graduate"
    @GetMapping("/{studentId}/graduation-eligibility")
    public ResponseEntity<String> checkGraduation(@PathVariable String studentId) {
        boolean isEligible = studentService.isEligibleToGraduate(studentId);
        if (isEligible) {
            return ResponseEntity.ok("Eligible to Graduate: All invoices paid.");
        } else {
            return ResponseEntity.status(403).body("Not Eligible: Outstanding invoices found in Finance Service.");
        }
    }

    // 5. VIEW PROFILE: Requirement "view profile (includes student ID)"
    @GetMapping("/{id}")
    public ResponseEntity<Student> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

//     6. UPDATE PROFILE: Requirement "update name and surname"
    @PatchMapping("/{id}")
    public ResponseEntity<Student> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(studentService.updateStudent(id, updates));
    }

    // 7. VIEW ENROLMENTS: Requirement "View courses enrolled in"
    @GetMapping("/{id}/enrolments")
    public ResponseEntity<List<Enrollment>> getMyEnrolments(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentEnrolments(id));
    }

    @PostMapping("/mock-library/accounts")
    public ResponseEntity<String> mockLibraryAccount(@RequestBody Object dummy) {
        return ResponseEntity.ok("Mock Library Account Created");
    }

    @PostMapping("/mock-library/invoices")
    public ResponseEntity<String> mockLibraryInvoice(@RequestBody Object dummy) {
        return ResponseEntity.ok("Mock Library Invoice Created");
    }
}
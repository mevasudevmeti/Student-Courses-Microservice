package com.lbu.student_service.controller;

import com.lbu.student_service.clients.FinanceClient;
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

    // 2. VIEW COURSES: Requirement "View all the courses offered"
    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(studentService.getAllCourses());
    }

    // 3. ENROL: Requirement "Enrol in course"
    // Expects JSON like: {"studentId": 1, "courseId": 101}
    @PostMapping("/enrol")
    public ResponseEntity<Enrollment> enrol(@RequestParam Long studentId, @RequestParam Long courseId) {
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

    @PostMapping("/mock-library/accounts")
    public ResponseEntity<String> mockLibraryAccount(@RequestBody Object dummy) {
        return ResponseEntity.ok("Mock Library Account Created");
    }

    @PostMapping("/mock-library/invoices")
    public ResponseEntity<String> mockLibraryInvoice(@RequestBody Object dummy) {
        return ResponseEntity.ok("Mock Library Invoice Created");
    }
}
package com.lbu.student_service.controller;

import com.lbu.student_service.entities.Course;
import com.lbu.student_service.entities.Enrollment;
import com.lbu.student_service.entities.Student;
import com.lbu.student_service.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * This controller provides administrative endpoints for managing courses and viewing students.
 * Admin functions remain inside the Student microservice because they operate on student-owned data.
 */
@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8083"})
public class AdminController {

    private final StudentService studentService;

    public AdminController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/students")
    public ResponseEntity<List<Student>> getStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getStudentById(studentId));
    }

    @PatchMapping("/students/{studentId}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long studentId,
                                                 @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(studentService.updateStudent(studentId, updates));
    }

    @GetMapping("/students/{studentId}/enrolments")
    public ResponseEntity<List<Enrollment>> getStudentEnrolments(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getStudentEnrolments(studentId));
    }

    @GetMapping("/enrolments")
    public ResponseEntity<List<Enrollment>> getAllEnrolments() {
        return ResponseEntity.ok(studentService.getAllEnrolments());
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses() {
        return ResponseEntity.ok(studentService.getAllCourses());
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createCourse(course));
    }

    @PatchMapping("/courses/{courseId}")
    public ResponseEntity<Course> updateCourse(@PathVariable Long courseId,
                                               @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(studentService.updateCourse(courseId, updates));
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        studentService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}

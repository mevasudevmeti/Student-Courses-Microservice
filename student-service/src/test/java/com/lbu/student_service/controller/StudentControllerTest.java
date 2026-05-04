package com.lbu.student_service.controller;

import com.lbu.student_service.dto.AuthResponse;
import com.lbu.student_service.entities.Course;
import com.lbu.student_service.entities.Student;
import com.lbu.student_service.entities.User;
import com.lbu.student_service.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {

    @Mock
    private StudentService studentService;

    private StudentController controller;

    @BeforeEach
    void setUp() {
        controller = new StudentController(studentService);
    }

    @Test
    void registerReturnsCreatedStudent() {
        Student request = new Student(null, "Vasu", "Meti", "vasu@example.com", User.builder().build());
        Student saved = new Student(1L, "Vasu", "Meti", "vasu@example.com", User.builder().build());
        when(studentService.register(request)).thenReturn(saved);

        ResponseEntity<Student> response = controller.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getStudentId());
    }

    @Test
    void loginReturnsOkForValidCredentials() {
        User user = User.builder().userId(1L).username("vasu").role("STUDENT").build();
        Student student = new Student(10L, "Vasu", "Meti", "vasu@example.com", user);
        AuthResponse authResponse = AuthResponse.from(user, student);
        when(studentService.login("vasu", "password")).thenReturn(authResponse);

        ResponseEntity<?> response = controller.login(Map.of("username", "vasu", "password", "password"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authResponse, response.getBody());
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() {
        when(studentService.login("vasu", "wrong")).thenReturn(null);

        ResponseEntity<?> response = controller.login(Map.of("username", "vasu", "password", "wrong"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getAllCoursesReturnsCoursesFromService() {
        List<Course> courses = List.of(new Course(1L, "Software Engineering for Service Computing", 1200.0));
        when(studentService.getAllCourses()).thenReturn(courses);

        ResponseEntity<List<Course>> response = controller.getAllCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Software Engineering for Service Computing", response.getBody().get(0).getTitle());
    }

    @Test
    void enrolReturnsCreatedAndDelegatesToService() {
        Map<String, Object> enrolmentResult = Map.of("status", "SUCCESS", "invoiceReference", "123");
        when(studentService.enrolInCourse(1L, 2L)).thenReturn(enrolmentResult);

        ResponseEntity<Map<String, Object>> response = controller.enrol(1L, 2L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().get("status"));
        verify(studentService).enrolInCourse(1L, 2L);
    }

    @Test
    void graduationCheckReturnsEligibilityMessage() {
        when(studentService.isEligibleToGraduate(1L)).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.checkGraduation(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().get("eligible"));
        assertNotNull(response.getBody().get("message"));
    }
}

package com.lbu.student_service.service;

import com.lbu.student_service.entities.Student;
import com.lbu.student_service.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private StudentService studentService;

    Student student;
    @BeforeEach
    void setUp() {
        student = new Student(123L, "Vasu", "Meti", "meti@gmail.com");
    }

    @Test
    void getStudentById() {
        when(studentRepository.findById(student.getStudentId())).thenReturn(Optional.of(student));

        Student student1 = studentService.getStudentById(student.getStudentId());

        assertEquals(student.getStudentId(), student1.getStudentId());
    }

    @Test
    void saveStudent() {
        when(studentRepository.save(student)).thenReturn(student);

        Student savedStudent = studentService.saveStudent(student);

        assertEquals(student, savedStudent);
    }

    @Test
    void updateStudent() {
        Student updateStudentName = new Student(123L, "Vasu32", "Meti", "meti@gmail.com");

        when(studentRepository.existsById(student.getStudentId())).thenReturn(true);

        when(studentRepository.save(updateStudentName)).thenReturn(updateStudentName);

        Student updatesResult = studentService.updateStudent(updateStudentName, 123L);
        assertEquals(updateStudentName, updatesResult);
        assertEquals("Vasu32", updatesResult.getFirstName());
    }

    @Test
    void getAllStudents() {
    }
}
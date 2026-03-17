package com.lbu.student_service.service;

import com.lbu.student_service.entities.Student;
import com.lbu.student_service.exception.StudentNotFoundException;
import com.lbu.student_service.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudent(Student student, long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException("Student not found with id: " + studentId);
        }
        student.setStudentId(studentId);
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public void deleteStudentById(Long studentId) {
        if(!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException("Student not found with id: " + studentId);
        }
        studentRepository.deleteById(studentId);
    }
}
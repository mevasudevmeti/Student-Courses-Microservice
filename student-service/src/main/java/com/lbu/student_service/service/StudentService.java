package com.lbu.student_service.service;

import com.lbu.student_service.clients.FinanceClient;
import com.lbu.student_service.clients.LibraryClient;
import com.lbu.student_service.dto.*;
import com.lbu.student_service.entities.*;
import com.lbu.student_service.exception.StudentNotFoundException;
import com.lbu.student_service.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FinanceClient financeClient;
    private final LibraryClient libraryClient;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          FinanceClient financeClient,
                          LibraryClient libraryClient,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.financeClient = financeClient;
        this.libraryClient = libraryClient;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Student login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return studentRepository.findByUser(user);
        }
        return null;
    }

    /**
     * Requirement: "Enrol in course" + "First enrolment account creation"
     * This method handles the logic of checking services and generating a real invoice ref.
     */
    @Transactional
    public Map<String, Object> enrolInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        AccountDto externalAccount = new AccountDto();
        externalAccount.setStudentId(student.getStudentId().toString());

        // 1. Ensure accounts exist in Finance and Library
        try {
            financeClient.createAccount(externalAccount);
        } catch (Exception e) {
            System.out.println("Finance account exists or skipped: " + e.getMessage());
        }

        try {
            libraryClient.createLibraryAccount(externalAccount);
        } catch (Exception e) {
            System.out.println("Library account skipped: " + e.getMessage());
        }

        // 2. Create the Invoice in Finance Service and get the 8-char Reference
        String invoiceReference = "PENDING";
        try {
            InvoiceDto invoiceRequest = new InvoiceDto();
            invoiceRequest.setStudentId(student.getStudentId().toString());
            invoiceRequest.setAmount(course.getPrice());
            invoiceRequest.setDueDate(LocalDate.now().plusDays(30));

            // This now returns the InvoiceDto with the reference
            InvoiceDto response = financeClient.createInvoice(invoiceRequest);
            if (response != null && response.getReference() != null) {
                invoiceReference = response.getReference();
            }
        } catch (Exception e) {
            System.err.println("Failed to generate Finance invoice: " + e.getMessage());
        }

        // 3. Save local enrollment linked to the invoice reference
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setInvoiceReference(invoiceReference);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 4. Return Data for React UI Alert
        Map<String, Object> result = new HashMap<>();
        result.put("enrollmentId", savedEnrollment.getId());
        result.put("courseTitle", course.getTitle());
        result.put("invoiceReference", invoiceReference);
        result.put("amount", course.getPrice());
        result.put("status", "SUCCESS");

        return result;
    }

    public Student register(Student student) {
        if (student.getUser().getRole() == null) {
            student.getUser().setRole("STUDENT");
        }
        student.getUser().setPassword(passwordEncoder.encode(student.getUser().getPassword()));

        User savedUser = userRepository.save(student.getUser());
        student.setUser(savedUser);
        Student savedStudent = studentRepository.save(student);

        String sid = savedStudent.getStudentId().toString();
        AccountDto accountDto = new AccountDto();
        accountDto.setStudentId(sid);

        try {
            financeClient.createAccount(accountDto);
        } catch (Exception e) {
            System.err.println("Finance account creation failed: " + e.getMessage());
        }

        try {
            libraryClient.createLibraryAccount(accountDto);
        } catch (Exception e) {
            System.err.println("Library account creation failed: " + e.getMessage());
        }

        return savedStudent;
    }

    public boolean isEligibleToGraduate(String studentId) {
        try {
            AccountDto account = financeClient.getAccountByStudentId(studentId);
            if (account == null) return false;

            // If they have an outstanding balance, they cannot graduate
            return !account.isHasOutstandingBalance();
        } catch (Exception e) {
            System.err.println("Finance Service Error: " + e.getMessage());
            return false;
        }
    }

    public Student updateStudent(Long id, Map<String, String> updates) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (updates.containsKey("firstName")) student.setFirstName(updates.get("firstName"));
        if (updates.containsKey("lastName")) student.setLastName(updates.get("lastName"));

        return studentRepository.save(student);
    }

    public List<Enrollment> getStudentEnrolments(Long id) {
        return enrollmentRepository.findByStudentId(id);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException("Not found"));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}
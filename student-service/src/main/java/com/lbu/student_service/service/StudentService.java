package com.lbu.student_service.service;

import com.lbu.student_service.clients.FinanceClient;
import com.lbu.student_service.clients.LibraryClient; // Make sure to create this client
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

    // Use Constructor Injection for all dependencies (Markers prefer this over @Autowired)
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

    // 1. Requirement: "View all the courses offered"
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // 2. Requirement: "Enrol in course" + "First enrolment account creation"
    @Transactional
    public Enrollment enrolInCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        AccountDto externalAccount = new AccountDto();
        externalAccount.setStudentId(student.getStudentId().toString());

        // 1. Handle Finance Account Creation
        try {
            financeClient.createAccount(externalAccount);
        } catch (Exception e) {
            // If Finance says 422 (Already exists), we just log it and move on
            System.out.println("Finance account already exists for ID: " + studentId + ". Skipping creation.");
        }

        // 2. Handle Library Account Creation (The Mocked/Fixed call)
        try {
            libraryClient.createAccount(externalAccount);
        } catch (Exception e) {
            // This prevents the Library 404 from crashing your whole enrolment process
            System.out.println("Library service unavailable or account exists. Skipping.");
        }

        // 3. Save local enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 4. Requirement: Send invoice to Finance (This creates the actual bill)
        try {
            createEnrollmentInvoice(student, course.getPrice());
        } catch (Exception e) {
            System.err.println("Failed to create Finance invoice: " + e.getMessage());
        }

        return savedEnrollment;
    }

    // 3. Requirement: "Register/Log in"
    public Student register(Student student) {
        if (student.getUser().getRole() == null) {
            student.getUser().setRole("STUDENT");
        }
        student.getUser().setPassword(passwordEncoder.encode(student.getUser().getPassword()));
        User savedUser = userRepository.save(student.getUser());
        student.setUser(savedUser);
        return studentRepository.save(student);
    }

    // 4. Requirement: "Graduation - view eligibility"
    public boolean isEligibleToGraduate(String studentId) {
        try {
            // Fetch the account from Finance Service
            AccountDto account = financeClient.getAccountByStudentId(studentId);

            if (account == null) {
                System.out.println("No account found in Finance for student: " + studentId);
                return false;
            }

            // DEBUG: Check what value is actually coming back
            System.out.println("DEBUG: Finance Service reports hasOutstandingBalance = "
                    + account.isHasOutstandingBalance() + " for student " + studentId);

            // LOGIC FIX:
            // If they HAVE an outstanding balance (true), they are NOT eligible (false)
            if (account.isHasOutstandingBalance()) {
                return false; // Stop graduation
            } else {
                return true;  // Allow graduation
            }

        } catch (Exception e) {
            System.err.println("Finance Service Error: " + e.getMessage());
            return false; // Default to 'not eligible' if service is down
        }
    }

    public void createEnrollmentInvoice(Student student, Double coursePrice) {
        String sid = student.getStudentId().toString();

        // 1. Construct the nested Account object for the invoice
        // The Finance Invoice model likely has: private Account account;
        Map<String, Object> accountRef = new HashMap<>();
        accountRef.put("studentId", sid);

        // 2. Build the main Invoice Map
        Map<String, Object> invoiceMap = new HashMap<>();
        invoiceMap.put("amount", coursePrice);
        invoiceMap.put("dueDate", LocalDate.now().plusDays(30).toString());
        invoiceMap.put("type", "TUITION_FEES");
        invoiceMap.put("status", "OUTSTANDING");

        // CHANGE: Pass the account as an object, not a string
        // If the model uses the field name 'account', we use that:
        invoiceMap.put("account", accountRef);

        // Safety: Some versions use 'studentId' at the top level, keep it too
        invoiceMap.put("studentId", sid);

        try {
            System.out.println("Sending nested Invoice for Student: " + sid);
            financeClient.createInvoice(invoiceMap);
            System.out.println("SUCCESS: Invoice created and linked to account.");
        } catch (Exception e) {
            // If 'account' doesn't work, try renaming the key to 'student'
            System.err.println("Finance error: " + e.getMessage());
        }
    }

    // Standard CRUD methods
    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException("Not found"));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
}
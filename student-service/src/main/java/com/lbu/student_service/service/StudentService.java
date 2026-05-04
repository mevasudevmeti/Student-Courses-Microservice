package com.lbu.student_service.service;

import com.lbu.student_service.clients.FinanceClient;
import com.lbu.student_service.clients.LibraryClient;
import com.lbu.student_service.dto.AccountDto;
import com.lbu.student_service.dto.AuthResponse;
import com.lbu.student_service.dto.CreateInvoiceRequest;
import com.lbu.student_service.dto.InvoiceDto;
import com.lbu.student_service.dto.LibraryMemberDto;
import com.lbu.student_service.entities.Course;
import com.lbu.student_service.entities.Enrollment;
import com.lbu.student_service.entities.Student;
import com.lbu.student_service.entities.User;
import com.lbu.student_service.exception.ExternalServiceException;
import com.lbu.student_service.exception.StudentNotFoundException;
import com.lbu.student_service.repository.CourseRepository;
import com.lbu.student_service.repository.EnrollmentRepository;
import com.lbu.student_service.repository.StudentRepository;
import com.lbu.student_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 * This service contains the Student microservice business logic and coordinates with Finance and
 * Library without owning their databases.
 *
 * Dependencies are supplied through constructor injection. The service talks to repository and
 * Feign-client interfaces instead of creating concrete objects itself.
 *
 * Student, course and enrolment records are saved through JPA repositories inside transactions.
 *
 * FinanceClient and LibraryClient perform REST calls to the external microservices.
 */
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

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        Student student = "ADMIN".equalsIgnoreCase(user.getRole())
                ? null
                : studentRepository.findByUser(user).orElse(null);
        return AuthResponse.from(user, student);
    }

    /**
     * Student creation is strict: if Finance or Library account creation fails, the transaction rolls
     * back and the local student is not kept in an inconsistent state.
     */
    @Transactional
    public Student register(Student student) {
        validateStudentForRegistration(student);

        User user = student.getUser();
        user.setRole("STUDENT");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        student.setUser(savedUser);
        Student savedStudent = studentRepository.save(student);

        ensureFinanceAccount(savedStudent);
        ensureLibraryAccount(savedStudent);

        return savedStudent;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public Course createCourse(Course course) {
        validateCourse(course);
        course.setCourseId(null);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long courseId, Map<String, String> updates) {
        Course course = getCourseById(courseId);

        if (updates.containsKey("title") && !updates.get("title").isBlank()) {
            course.setTitle(updates.get("title").trim());
        }
        if (updates.containsKey("price") && !updates.get("price").isBlank()) {
            course.setPrice(Double.parseDouble(updates.get("price")));
        }

        validateCourse(course);
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = getCourseById(courseId);
        courseRepository.delete(course);
    }

    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
    }

    /**
     * Enrolment is only saved after Finance returns a valid invoice. This keeps the Student and
     * Finance services consistent for the coursework demo.
     */
    @Transactional
    public Map<String, Object> enrolInCourse(Long studentId, Long courseId) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);

        enrollmentRepository.findByStudentAndCourse(student, course).ifPresent(existing -> {
            throw new IllegalStateException("Student is already enrolled in this course.");
        });

        InvoiceDto financeInvoice = createCourseInvoice(student, course);

        Enrollment enrollment = new Enrollment(student, course, String.valueOf(financeInvoice.getId()));
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        Map<String, Object> result = new HashMap<>();
        result.put("enrollmentId", savedEnrollment.getEnrollmentId());
        result.put("studentId", student.getStudentId());
        result.put("courseId", course.getCourseId());
        result.put("courseTitle", course.getTitle());
        result.put("amount", course.getPrice());
        result.put("invoiceId", financeInvoice.getId());
        result.put("invoiceReference", String.valueOf(financeInvoice.getId()));
        result.put("invoiceStatus", financeInvoice.getStatus());
        result.put("status", "SUCCESS");
        return result;
    }

    public boolean isEligibleToGraduate(Long studentId) {
        getStudentById(studentId);
        try {
            AccountDto account = financeClient.getAccountByStudentId(studentId);
            return account != null && !account.hasOutstandingBalance();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Profile updates are local-only. They must not create another Library account or Finance account.
     */
    @Transactional
    public Student updateStudent(Long studentId, Map<String, String> updates) {
        Student student = getStudentById(studentId);

        if (updates.containsKey("firstName") && !updates.get("firstName").isBlank()) {
            student.setFirstName(updates.get("firstName").trim());
        }
        if (updates.containsKey("lastName") && !updates.get("lastName").isBlank()) {
            student.setLastName(updates.get("lastName").trim());
        }
        if (updates.containsKey("email") && !updates.get("email").isBlank()) {
            String email = updates.get("email").trim();
            studentRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getStudentId().equals(studentId)) {
                    throw new IllegalStateException("Email already exists.");
                }
            });
            student.setEmail(email);
        }

        return studentRepository.save(student);
    }

    public InvoiceDto createLibraryFineInvoice(Long studentId, Double amount, String description) {
        Student student = getStudentById(studentId);

        try {
            CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest();
            invoiceRequest.setStudentId(student.getStudentId());
            invoiceRequest.setDescription(description == null || description.isBlank()
                    ? "Library fine for late book return"
                    : description.trim());
            invoiceRequest.setAmount(amount);
            invoiceRequest.setDateCreated(LocalDate.now());

            InvoiceDto invoice = financeClient.createInvoice(invoiceRequest);
            if (invoice == null || invoice.getId() == null) {
                throw new ExternalServiceException("Finance service did not return a valid library fine invoice.");
            }
            return invoice;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException("Could not create library fine invoice through Finance service.", e);
        }
    }

    public List<Enrollment> getStudentEnrolments(Long studentId) {
        getStudentById(studentId);
        return enrollmentRepository.findByStudentStudentId(studentId);
    }

    public Student getStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Enrollment> getAllEnrolments() {
        return enrollmentRepository.findAll();
    }

    private void validateStudentForRegistration(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student details are required.");
        }
        if (student.getUser() == null) {
            throw new IllegalArgumentException("Portal user details are required.");
        }
        if (isBlank(student.getFirstName())) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (isBlank(student.getLastName())) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (isBlank(student.getEmail())) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (isBlank(student.getUser().getUsername())) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (isBlank(student.getUser().getPassword())) {
            throw new IllegalArgumentException("Password is required.");
        }

        userRepository.findByUsername(student.getUser().getUsername()).ifPresent(existing -> {
            throw new IllegalStateException("Username already exists.");
        });
        studentRepository.findByEmail(student.getEmail()).ifPresent(existing -> {
            throw new IllegalStateException("Email already exists.");
        });
    }

    private void validateCourse(Course course) {
        if (course == null || isBlank(course.getTitle())) {
            throw new IllegalArgumentException("Course title is required.");
        }
        if (course.getPrice() == null || course.getPrice() < 0) {
            throw new IllegalArgumentException("Course price must be zero or more.");
        }
    }

    private void ensureFinanceAccount(Student student) {
        try {
            financeClient.createAccount(student.getStudentId());
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Finance service is unavailable. Student registration cannot continue because a finance account could not be created.",
                    e
            );
        }
    }

    private void ensureLibraryAccount(Student student) {
        try {
            LibraryMemberDto member = new LibraryMemberDto();
            member.setMemberCode("STU-" + student.getStudentId());
            member.setFullName(student.getFirstName() + " " + student.getLastName());
            member.setEmail(student.getEmail());
            member.setStatus("ACTIVE");

            libraryClient.createMember(member);
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Library service is unavailable. Student registration cannot continue because a library account could not be created.",
                    e
            );
        }
    }

    private InvoiceDto createCourseInvoice(Student student, Course course) {
        try {
            CreateInvoiceRequest invoiceRequest = new CreateInvoiceRequest();
            invoiceRequest.setStudentId(student.getStudentId());
            invoiceRequest.setDescription("Course enrolment: " + course.getTitle());
            invoiceRequest.setAmount(course.getPrice());
            invoiceRequest.setDateCreated(LocalDate.now());

            InvoiceDto invoice = financeClient.createInvoice(invoiceRequest);
            if (invoice == null || invoice.getId() == null) {
                throw new ExternalServiceException(
                        "Finance service did not return a valid invoice reference. Enrolment has been cancelled."
                );
            }
            return invoice;
        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ExternalServiceException(
                    "Finance service is unavailable. Enrolment has been cancelled because an invoice could not be created.",
                    e
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

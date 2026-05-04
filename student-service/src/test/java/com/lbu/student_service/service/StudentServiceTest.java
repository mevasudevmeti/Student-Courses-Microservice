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
import com.lbu.student_service.repository.CourseRepository;
import com.lbu.student_service.repository.EnrollmentRepository;
import com.lbu.student_service.repository.StudentRepository;
import com.lbu.student_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private FinanceClient financeClient;

    @Mock
    private LibraryClient libraryClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(
                studentRepository,
                userRepository,
                courseRepository,
                enrollmentRepository,
                financeClient,
                libraryClient,
                passwordEncoder
        );
    }

    @Test
    void registerCreatesLocalStudentAndExternalFinanceAndLibraryAccounts() {
        User newUser = User.builder().username("vasu").password("plain-password").build();
        Student newStudent = new Student(null, "Vasu", "Meti", "vasu@example.com", newUser);
        User savedUser = User.builder().userId(10L).username("vasu").password("encoded-password").role("STUDENT").build();
        Student savedStudent = new Student(1L, "Vasu", "Meti", "vasu@example.com", savedUser);

        when(userRepository.findByUsername("vasu")).thenReturn(Optional.empty());
        when(studentRepository.findByEmail("vasu@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);
        when(financeClient.createAccount(1L)).thenReturn(new AccountDto());
        when(libraryClient.createMember(any(LibraryMemberDto.class))).thenReturn(new LibraryMemberDto());

        Student result = studentService.register(newStudent);

        assertEquals(1L, result.getStudentId());
        assertEquals("Vasu", result.getFirstName());
        assertEquals("STUDENT", newUser.getRole());
        assertEquals("encoded-password", newUser.getPassword());
        verify(financeClient).createAccount(1L);
        verify(libraryClient).createMember(any(LibraryMemberDto.class));
    }

    @Test
    void loginReturnsAuthResponseForValidStudentCredentials() {
        User user = User.builder().userId(5L).username("student1").password("encoded").role("STUDENT").build();
        Student student = new Student(9L, "Asha", "Patel", "asha@example.com", user);

        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(studentRepository.findByUser(user)).thenReturn(Optional.of(student));

        AuthResponse response = studentService.login("student1", "secret");

        assertNotNull(response);
        assertEquals("student1", response.getUsername());
        assertEquals("STUDENT", response.getRole());
        assertEquals(9L, response.getStudent().getStudentId());
    }

    @Test
    void loginReturnsNullForInvalidCredentials() {
        User user = User.builder().username("student1").password("encoded").role("STUDENT").build();
        when(userRepository.findByUsername("student1")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        AuthResponse response = studentService.login("student1", "wrong");

        assertEquals(null, response);
    }

    @Test
    void enrolInCourseCreatesFinanceInvoiceBeforeSavingEnrollment() {
        User user = User.builder().userId(1L).username("student1").password("encoded").role("STUDENT").build();
        Student student = new Student(1L, "Vasu", "Meti", "vasu@example.com", user);
        Course course = new Course(2L, "Cloud Computing and Microservices", 950.00);
        InvoiceDto invoice = new InvoiceDto();
        invoice.setId(77L);
        invoice.setStatus("OUTSTANDING");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentAndCourse(student, course)).thenReturn(Optional.empty());
        when(financeClient.createInvoice(any(CreateInvoiceRequest.class))).thenReturn(invoice);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            enrollment.setEnrollmentId(100L);
            return enrollment;
        });

        Map<String, Object> result = studentService.enrolInCourse(1L, 2L);

        assertEquals(100L, result.get("enrollmentId"));
        assertEquals("77", result.get("invoiceReference"));
        assertEquals("SUCCESS", result.get("status"));

        ArgumentCaptor<CreateInvoiceRequest> invoiceRequestCaptor = ArgumentCaptor.forClass(CreateInvoiceRequest.class);
        verify(financeClient).createInvoice(invoiceRequestCaptor.capture());
        assertEquals(1L, invoiceRequestCaptor.getValue().getStudentId());
        assertEquals(950.00, invoiceRequestCaptor.getValue().getAmount());
        assertEquals("Course enrolment: Cloud Computing and Microservices", invoiceRequestCaptor.getValue().getDescription());
    }

    @Test
    void enrolInCourseDoesNotSaveEnrollmentWhenFinanceInvoiceFails() {
        Student student = new Student(1L, "Vasu", "Meti", "vasu@example.com", User.builder().build());
        Course course = new Course(2L, "Enterprise Web Applications", 990.00);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(2L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentAndCourse(student, course)).thenReturn(Optional.empty());
        when(financeClient.createInvoice(any(CreateInvoiceRequest.class))).thenThrow(new RuntimeException("Finance down"));

        assertThrows(ExternalServiceException.class, () -> studentService.enrolInCourse(1L, 2L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void updateStudentProfileOnlyUpdatesLocalStudentData() {
        Student student = new Student(1L, "Old", "Name", "old@example.com", User.builder().build());
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(studentRepository.save(student)).thenReturn(student);

        Student updated = studentService.updateStudent(1L, Map.of(
                "firstName", "New",
                "lastName", "Student",
                "email", "new@example.com"
        ));

        assertEquals("New", updated.getFirstName());
        assertEquals("Student", updated.getLastName());
        assertEquals("new@example.com", updated.getEmail());
        verify(financeClient, never()).createAccount(any());
        verify(libraryClient, never()).createMember(any());
    }

    @Test
    void graduationEligibilityReturnsTrueWhenFinanceAccountHasNoOutstandingBalance() {
        Student student = new Student(1L, "Vasu", "Meti", "vasu@example.com", User.builder().build());
        AccountDto account = new AccountDto();
        account.setStudentId(1L);
        account.setBalance(0.0);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(financeClient.getAccountByStudentId(1L)).thenReturn(account);

        assertTrue(studentService.isEligibleToGraduate(1L));
    }

    @Test
    void graduationEligibilityReturnsFalseWhenFinanceAccountHasOutstandingBalance() {
        Student student = new Student(1L, "Vasu", "Meti", "vasu@example.com", User.builder().build());
        AccountDto account = new AccountDto();
        account.setStudentId(1L);
        account.setBalance(50.0);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(financeClient.getAccountByStudentId(1L)).thenReturn(account);

        assertFalse(studentService.isEligibleToGraduate(1L));
    }
}

# Student-Courses-Microservice

The Student Service Portal is a Spring Boot microservice developed as part of a microservices-based online course application. It provides the main student-facing functionality, including student registration, login, viewing available courses, course enrolment, viewing enrolments, updating student profile details and checking graduation eligibility.

This service integrates with two other microservices: the Finance service and the Library service. These services are kept separate to follow a microservices architecture, where each service has its own responsibility and database.

### Finance Service Integration

The Student service communicates with the Finance service using REST client calls through OpenFeign. When a student account is created, the Student service sends a request to the Finance service to create a finance account for that student. When the student enrols in a course, the Student service sends a request to the Finance service to create an invoice for the course fee. The returned invoice reference is stored with the enrolment record. Graduation eligibility is also checked through the Finance service by verifying whether the student has any outstanding balance.

### Library Service Integration

The Student service also integrates with the Library service using REST client calls. When a student is created, the Student service sends a request to the Library service to create a library member account. This allows the student to be recognised by the Library service as part of the wider student portal system.

### Admin Functionality

The portal includes a default admin user who can log in to manage courses and view student-related information. The admin can add and delete courses, view registered students, view student profiles, check enrolments and review graduation eligibility.

### User Interface

A simple browser-based user interface is included under the `static` resources folder. Students can register, log in, view courses, enrol in courses, view their enrolments, update their profile and check graduation eligibility. The admin interface provides separate pages for managing courses and viewing student data.

This project demonstrates key service-computing concepts including service-oriented architecture, microservices, dependency injection, loose coupling, REST server endpoints, REST client communication, database persistence using Spring Data JPA and configuration management using `application.yaml`.

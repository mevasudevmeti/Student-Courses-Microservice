package com.lbu.student_service.config;

import com.lbu.student_service.entities.Course;
import com.lbu.student_service.entities.User;
import com.lbu.student_service.repository.CourseRepository;
import com.lbu.student_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * This startup component seeds initial database rows through Spring Data repositories.
 * Spring injects the repositories and password encoder into the bean method.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(CourseRepository courseRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (courseRepository.count() == 0) {
                Course softwareEngineering = new Course(null, "Software Engineering for Service Computing", 1200.00);
                Course cloudComputing = new Course(null, "Cloud Computing and Microservices", 950.00);
                Course databaseSystems = new Course(null, "Advanced Database Systems", 875.00);
                Course webApplications = new Course(null, "Enterprise Web Applications", 990.00);

                courseRepository.saveAll(List.of(
                        softwareEngineering,
                        cloudComputing,
                        databaseSystems,
                        webApplications
                ));
            }

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .role("ADMIN")
                        .build();
                userRepository.save(admin);
            }
        };
    }
}

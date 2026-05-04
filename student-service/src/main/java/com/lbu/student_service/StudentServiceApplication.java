package com.lbu.student_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * This is the independent Student microservice. It owns student, course and enrolment data,
 * while communicating with Finance and Library as separate services.
 *
 * @EnableFeignClients activates OpenFeign interfaces so this service can call other REST services.
 *
 * Runtime settings such as service URLs, port and database connection are read from application.yaml.
 */
@SpringBootApplication
@EnableFeignClients
public class StudentServiceApplication {

	public static void main(String[] args) {

		SpringApplication.run(StudentServiceApplication.class, args);
		System.out.println("Student Service Application Started");
	}

}

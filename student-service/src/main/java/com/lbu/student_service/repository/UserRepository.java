package com.lbu.student_service.repository;

import com.lbu.student_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * This repository looks up portal login accounts from the portal_users table.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}

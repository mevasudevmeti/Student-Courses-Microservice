package com.leedsbeckett.financeservice.repository;

import com.leedsbeckett.financeservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByStudentId(Long studentId);
}
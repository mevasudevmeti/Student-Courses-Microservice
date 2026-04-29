package com.leedsbeckett.financeservice.controller;

import com.leedsbeckett.financeservice.entity.Account;
import com.leedsbeckett.financeservice.service.AccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{studentId}")
    public Account createAccount(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateCreated
    ) {
        return accountService.createAccount(studentId, dateCreated);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{studentId}")
    public Account getAccount(@PathVariable Long studentId) {
        return accountService.getAccount(studentId);
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long studentId) {
        accountService.deleteAccount(studentId);
        return ResponseEntity.noContent().build();
    }
}

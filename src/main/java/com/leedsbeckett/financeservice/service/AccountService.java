package com.leedsbeckett.financeservice.service;

import com.leedsbeckett.financeservice.entity.Account;
import com.leedsbeckett.financeservice.entity.Invoice;
import com.leedsbeckett.financeservice.exception.AccountNotFoundException;
import com.leedsbeckett.financeservice.exception.AccountNotValidException;
import com.leedsbeckett.financeservice.repository.AccountRepository;
import com.leedsbeckett.financeservice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final InvoiceRepository invoiceRepository;

    public AccountService(AccountRepository accountRepository, InvoiceRepository invoiceRepository) {
        this.accountRepository = accountRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public Account createAccount(Long studentId, LocalDate dateCreated) {
        Account existing = accountRepository.findByStudentId(studentId).orElse(null);
        if (existing != null) {
            return existing;
        }

        Account account = new Account();
        account.setStudentId(studentId);
        account.setBalance(0.0);
        account.setDateCreated(dateCreated != null ? dateCreated : LocalDate.now());
        return accountRepository.save(account);
    }

    public Account getAccount(Long studentId) {
        return accountRepository.findByStudentId(studentId)
                .orElseThrow(AccountNotFoundException::new);
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public void deleteAccount(Long studentId) {
        Account account = accountRepository.findByStudentId(studentId)
                .orElseThrow(AccountNotFoundException::new);

        List<Invoice> invoices = invoiceRepository.findByStudentId(studentId);

        if (!invoices.isEmpty()) {
            throw new AccountNotValidException("Cannot delete account with existing invoices");
        }

        accountRepository.delete(account);
    }
}

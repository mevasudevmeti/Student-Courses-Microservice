package com.leedsbeckett.financeservice.service;

import com.leedsbeckett.financeservice.model.CreateInvoiceRequest;
import com.leedsbeckett.financeservice.model.UpdateInvoiceRequest;
import com.leedsbeckett.financeservice.entity.Account;
import com.leedsbeckett.financeservice.entity.Invoice;
import com.leedsbeckett.financeservice.repository.AccountRepository;
import com.leedsbeckett.financeservice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceService {

    private final AccountRepository accountRepository;
    private final InvoiceRepository invoiceRepository;

    public FinanceService(AccountRepository accountRepository, InvoiceRepository invoiceRepository) {
        this.accountRepository = accountRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public Account createAccount(Long studentId) {
        Account existing = accountRepository.findByStudentId(studentId).orElse(null);
        if (existing != null) {
            return existing;
        }

        Account account = new Account();
        account.setStudentId(studentId);
        account.setBalance(0.0);
        return accountRepository.save(account);
    }

    public Account getAccount(Long studentId) {
        return accountRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
    }

    public Invoice createInvoice(CreateInvoiceRequest request) {
        Account account = accountRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Invoice invoice = new Invoice();
        invoice.setStudentId(request.getStudentId());
        invoice.setDescription(request.getDescription());
        invoice.setAmount(request.getAmount());
        invoice.setStatus("UNPAID");

        account.setBalance(account.getBalance() + request.getAmount());
        accountRepository.save(account);

        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(Long invoiceId, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!"UNPAID".equals(invoice.getStatus())) {
            throw new RuntimeException("Only unpaid invoices can be updated");
        }

        Account account = accountRepository.findByStudentId(invoice.getStudentId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        double updatedAmount = request.getAmount() != null ? request.getAmount() : invoice.getAmount();
        double amountDelta = updatedAmount - invoice.getAmount();

        if (request.getDescription() != null) {
            invoice.setDescription(request.getDescription());
        }
        invoice.setAmount(updatedAmount);

        account.setBalance(account.getBalance() + amountDelta);
        accountRepository.save(account);

        return invoiceRepository.save(invoice);
    }

    public Invoice payInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!"UNPAID".equals(invoice.getStatus())) {
            throw new RuntimeException("Only unpaid invoices can be paid");
        }

        Account account = accountRepository.findByStudentId(invoice.getStudentId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance() - invoice.getAmount());
        accountRepository.save(account);

        invoice.setStatus("PAID");
        return invoiceRepository.save(invoice);
    }

    public Invoice cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!"UNPAID".equals(invoice.getStatus())) {
            throw new RuntimeException("Only unpaid invoices can be cancelled");
        }

        Account account = accountRepository.findByStudentId(invoice.getStudentId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance() - invoice.getAmount());
        accountRepository.save(account);

        invoice.setStatus("CANCELLED");
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if ("UNPAID".equals(invoice.getStatus())) {
            Account account = accountRepository.findByStudentId(invoice.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            account.setBalance(account.getBalance() - invoice.getAmount());
            accountRepository.save(account);
        }

        invoiceRepository.delete(invoice);
    }

    public void deleteAccount(Long studentId) {
        Account account = accountRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<Invoice> invoices = invoiceRepository.findByStudentId(studentId);

        if (!invoices.isEmpty()) {
            throw new RuntimeException("Cannot delete account with existing invoices");
        }

        accountRepository.delete(account);
    }
}

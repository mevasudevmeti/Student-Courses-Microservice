package com.leedsbeckett.financeservice.controller;

import com.leedsbeckett.financeservice.model.CreateInvoiceRequest;
import com.leedsbeckett.financeservice.model.UpdateInvoiceRequest;
import com.leedsbeckett.financeservice.entity.Account;
import com.leedsbeckett.financeservice.entity.Invoice;
import com.leedsbeckett.financeservice.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @PostMapping("/accounts/{studentId}")
    public Account createAccount(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateCreated
    ) {
        return financeService.createAccount(studentId, dateCreated);
    }

    @GetMapping("/accounts")
    public List<Account> getAllAccounts() {
        return financeService.getAllAccounts();
    }

    @GetMapping("/accounts/{studentId}")
    public Account getAccount(@PathVariable Long studentId) {
        return financeService.getAccount(studentId);
    }

    @DeleteMapping("/accounts/{studentId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long studentId) {
        financeService.deleteAccount(studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/invoices")
    public List<Invoice> getAllInvoices() {
        return financeService.getAllInvoices();
    }

    @GetMapping("/invoices/{id}")
    public Invoice getInvoiceById(@PathVariable Long id) {
        return financeService.getInvoiceById(id);
    }

    @PostMapping("/invoices")
    public Invoice createInvoice(@RequestBody CreateInvoiceRequest request) {
        return financeService.createInvoice(request);
    }

    @PutMapping("/invoices/{id}")
    public Invoice updateInvoice(@PathVariable Long id, @RequestBody UpdateInvoiceRequest request) {
        return financeService.updateInvoice(id, request);
    }

    @PutMapping("/invoices/{id}/pay")
    public Invoice payInvoice(@PathVariable Long id) {
        return financeService.payInvoice(id);
    }

    @PutMapping("/invoices/{id}/cancel")
    public Invoice cancelInvoice(@PathVariable Long id) {
        return financeService.cancelInvoice(id);
    }

    @DeleteMapping("/invoices/{invoiceId}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long invoiceId) {
        financeService.deleteInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }
}

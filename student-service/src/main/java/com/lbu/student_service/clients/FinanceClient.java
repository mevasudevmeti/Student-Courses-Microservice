package com.lbu.student_service.clients;

import com.lbu.student_service.dto.AccountDto;
import com.lbu.student_service.dto.InvoiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "finance-service", url = "http://localhost:8081")
public interface FinanceClient {

    @PostMapping("/accounts")
    void createAccount(@RequestBody AccountDto accountData);

    @PostMapping("/invoices")
    void createInvoice(@RequestBody Map<String, Object> invoiceData);

    /**
     * Fetches the account details including the 'hasOutstandingBalance' flag.
     */
    @GetMapping("/accounts/student/{studentId}")
    AccountDto getAccountByStudentId(@PathVariable("studentId") String studentId);
}
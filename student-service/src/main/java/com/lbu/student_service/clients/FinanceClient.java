package com.lbu.student_service.clients;

import com.lbu.student_service.dto.AccountDto;
import com.lbu.student_service.dto.CreateInvoiceRequest;
import com.lbu.student_service.dto.InvoiceDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * This Feign client hides HTTP details behind a Java interface. StudentService can call Finance
 * methods without manually building URLs or RestTemplate calls.
 *
 * The service depends on this interface instead of a concrete HTTP implementation, making the code
 * easier to test and change.
 *
 * The Finance service URL is injected from services.finance.url in application.yaml.
 */
@FeignClient(name = "finance-service", url = "${services.finance.url}")
public interface FinanceClient {

    @PostMapping("/api/finance/accounts/{studentId}")
    AccountDto createAccount(@PathVariable("studentId") Long studentId);

    @GetMapping("/api/finance/accounts/{studentId}")
    AccountDto getAccountByStudentId(@PathVariable("studentId") Long studentId);

    @PostMapping("/api/finance/invoices")
    InvoiceDto createInvoice(@RequestBody CreateInvoiceRequest invoiceData);
}

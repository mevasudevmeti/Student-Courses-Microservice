package com.leedsbeckett.financeservice.controller;

import com.leedsbeckett.financeservice.entity.Invoice;
import com.leedsbeckett.financeservice.model.CreateInvoiceRequest;
import com.leedsbeckett.financeservice.model.UpdateInvoiceRequest;
import com.leedsbeckett.financeservice.service.InvoiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/finance/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<Invoice> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/{id}")
    public Invoice getInvoiceById(@PathVariable Long id) {
        return invoiceService.getInvoiceById(id);
    }

    @PostMapping
    public Invoice createInvoice(@RequestBody CreateInvoiceRequest request) {
        return invoiceService.createInvoice(request);
    }

    @PutMapping("/{id}")
    public Invoice updateInvoice(@PathVariable Long id, @RequestBody UpdateInvoiceRequest request) {
        return invoiceService.updateInvoice(id, request);
    }

    @PutMapping("/{id}/pay")
    public Invoice payInvoice(@PathVariable Long id) {
        return invoiceService.payInvoice(id);
    }

    @PutMapping("/{id}/cancel")
    public Invoice cancelInvoice(@PathVariable Long id) {
        return invoiceService.cancelInvoice(id);
    }

    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }
}

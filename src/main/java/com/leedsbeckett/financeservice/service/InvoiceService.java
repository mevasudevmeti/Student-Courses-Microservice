package com.leedsbeckett.financeservice.service;

import com.leedsbeckett.financeservice.entity.Account;
import com.leedsbeckett.financeservice.entity.Invoice;
import com.leedsbeckett.financeservice.exception.AccountNotFoundException;
import com.leedsbeckett.financeservice.exception.InvoiceNotFoundException;
import com.leedsbeckett.financeservice.exception.InvoiceNotValidException;
import com.leedsbeckett.financeservice.model.CreateInvoiceRequest;
import com.leedsbeckett.financeservice.model.UpdateInvoiceRequest;
import com.leedsbeckett.financeservice.repository.AccountRepository;
import com.leedsbeckett.financeservice.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final AccountRepository accountRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(AccountRepository accountRepository, InvoiceRepository invoiceRepository) {
        this.accountRepository = accountRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(InvoiceNotFoundException::new);
    }

    public Invoice createInvoice(CreateInvoiceRequest request) {
        Account account = accountRepository.findByStudentId(request.getStudentId())
                .orElseThrow(AccountNotFoundException::new);

        Invoice invoice = new Invoice();
        invoice.setStudentId(request.getStudentId());
        invoice.setDescription(request.getDescription());
        invoice.setAmount(request.getAmount());
        invoice.setStatus("UNPAID");
        invoice.setDateCreated(request.getDateCreated() != null ? request.getDateCreated() : LocalDate.now());

        account.setBalance(account.getBalance() + request.getAmount());
        accountRepository.save(account);

        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(Long invoiceId, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(InvoiceNotFoundException::new);

        if (!"UNPAID".equals(invoice.getStatus())) {
            throw new InvoiceNotValidException("Only unpaid invoices can be updated");
        }

        Account account = accountRepository.findByStudentId(invoice.getStudentId())
                .orElseThrow(AccountNotFoundException::new);

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
                .orElseThrow(InvoiceNotFoundException::new);

        if (!"UNPAID".equals(invoice.getStatus())) {
            throw new InvoiceNotValidException("Only unpaid invoices can be paid");
        }

        Account account = accountRepository.findByStudentId(invoice.getStudentId())
                .orElseThrow(AccountNotFoundException::new);

        account.setBalance(account.getBalance() - invoice.getAmount());
        accountRepository.save(account);

        invoice.setStatus("PAID");
        return invoiceRepository.save(invoice);
    }

    public Invoice cancelInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(InvoiceNotFoundException::new);

        if (!"UNPAID".equals(invoice.getStatus())) {
            throw new InvoiceNotValidException("Only unpaid invoices can be cancelled");
        }

        Account account = accountRepository.findByStudentId(invoice.getStudentId())
                .orElseThrow(AccountNotFoundException::new);

        account.setBalance(account.getBalance() - invoice.getAmount());
        accountRepository.save(account);

        invoice.setStatus("CANCELLED");
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(InvoiceNotFoundException::new);

        if ("UNPAID".equals(invoice.getStatus())) {
            Account account = accountRepository.findByStudentId(invoice.getStudentId())
                    .orElseThrow(AccountNotFoundException::new);

            account.setBalance(account.getBalance() - invoice.getAmount());
            accountRepository.save(account);
        }

        invoiceRepository.delete(invoice);
    }
}

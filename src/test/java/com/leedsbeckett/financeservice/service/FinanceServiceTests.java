package com.leedsbeckett.financeservice.service;

import com.leedsbeckett.financeservice.entity.Account;
import com.leedsbeckett.financeservice.entity.Invoice;
import com.leedsbeckett.financeservice.model.UpdateInvoiceRequest;
import com.leedsbeckett.financeservice.repository.AccountRepository;
import com.leedsbeckett.financeservice.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private FinanceService financeService;

    @Test
    void updateInvoiceChangesDetailsAndAdjustsBalanceForUnpaidInvoice() {
        Account account = new Account(1L, 1001L, 250.0);
        Invoice invoice = new Invoice(10L, 1001L, "Library fine", 50.0, "UNPAID");
        UpdateInvoiceRequest request = new UpdateInvoiceRequest();
        request.setDescription("Updated library fine");
        request.setAmount(80.0);

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(accountRepository.findByStudentId(1001L)).thenReturn(Optional.of(account));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Invoice updatedInvoice = financeService.updateInvoice(10L, request);

        assertEquals("Updated library fine", updatedInvoice.getDescription());
        assertEquals(80.0, updatedInvoice.getAmount());
        assertEquals(280.0, account.getBalance());
        verify(accountRepository).save(account);
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void updateInvoiceRejectsPaidInvoice() {
        Invoice invoice = new Invoice(11L, 1002L, "Tuition", 80.0, "PAID");
        UpdateInvoiceRequest request = new UpdateInvoiceRequest();
        request.setDescription("Updated tuition");
        request.setAmount(100.0);

        when(invoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> financeService.updateInvoice(11L, request));

        assertEquals("Only unpaid invoices can be updated", exception.getMessage());
        verify(accountRepository, never()).findByStudentId(1002L);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any(Account.class));
        verify(invoiceRepository, never()).save(invoice);
    }

    @Test
    void deleteInvoiceRemovesInvoiceAndUpdatesBalanceForUnpaidInvoice() {
        Account account = new Account(1L, 1001L, 250.0);
        Invoice invoice = new Invoice(10L, 1001L, "Library fine", 50.0, "UNPAID");

        when(invoiceRepository.findById(10L)).thenReturn(Optional.of(invoice));
        when(accountRepository.findByStudentId(1001L)).thenReturn(Optional.of(account));

        financeService.deleteInvoice(10L);

        assertEquals(200.0, account.getBalance());
        verify(accountRepository).save(account);
        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void deleteInvoiceRemovesPaidInvoiceWithoutChangingBalance() {
        Invoice invoice = new Invoice(11L, 1002L, "Tuition", 80.0, "PAID");

        when(invoiceRepository.findById(11L)).thenReturn(Optional.of(invoice));

        financeService.deleteInvoice(11L);

        verify(accountRepository, never()).findByStudentId(1002L);
        verify(accountRepository, never()).save(org.mockito.ArgumentMatchers.any(Account.class));
        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void deleteAccountDeletesAccountWhenNoInvoicesExist() {
        Account account = new Account(2L, 1003L, 0.0);

        when(accountRepository.findByStudentId(1003L)).thenReturn(Optional.of(account));
        when(invoiceRepository.findByStudentId(1003L)).thenReturn(List.of());

        financeService.deleteAccount(1003L);

        verify(accountRepository).delete(account);
    }

    @Test
    void deleteAccountRejectsWhenInvoicesStillExist() {
        Account account = new Account(3L, 1004L, 125.0);
        Invoice invoice = new Invoice(12L, 1004L, "Accommodation", 125.0, "UNPAID");

        when(accountRepository.findByStudentId(1004L)).thenReturn(Optional.of(account));
        when(invoiceRepository.findByStudentId(1004L)).thenReturn(List.of(invoice));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> financeService.deleteAccount(1004L));

        assertEquals("Cannot delete account with existing invoices", exception.getMessage());
        verify(accountRepository, never()).delete(account);
    }
}

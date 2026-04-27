package com.leedsbeckett.financeservice.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class CreateInvoiceRequest {

    private Long studentId;
    private String description;
    private Double amount;
    private LocalDate dateCreated;

    public CreateInvoiceRequest() {
    }

}

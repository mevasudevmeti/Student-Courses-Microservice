package com.leedsbeckett.financeservice.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateInvoiceRequest {

    private Long studentId;
    private String description;
    private Double amount;

    public CreateInvoiceRequest() {
    }

}
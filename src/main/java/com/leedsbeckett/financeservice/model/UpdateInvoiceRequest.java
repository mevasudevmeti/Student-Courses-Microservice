package com.leedsbeckett.financeservice.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateInvoiceRequest {

    private String description;
    private Double amount;

    public UpdateInvoiceRequest() {
    }
}

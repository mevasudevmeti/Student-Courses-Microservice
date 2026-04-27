package com.leedsbeckett.financeservice.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private String description;
    private Double amount;
    private String status;
    private LocalDate dateCreated;

    public Invoice() {
    }

    public Invoice(Long id, Long studentId, String description, Double amount, String status) {
        this.id = id;
        this.studentId = studentId;
        this.description = description;
        this.amount = amount;
        this.status = status;
    }

    public Invoice(Long id, Long studentId, String description, Double amount, String status, LocalDate dateCreated) {
        this.id = id;
        this.studentId = studentId;
        this.description = description;
        this.amount = amount;
        this.status = status;
        this.dateCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getDescription() {
        return description;
    }

    public Double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }
}

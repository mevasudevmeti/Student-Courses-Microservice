package com.leedsbeckett.financeservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Double balance;
    private LocalDate dateCreated;

    public Account() {
    }

    public Account(Long id, Long studentId, Double balance) {
        this.id = id;
        this.studentId = studentId;
        this.balance = balance;
    }

    public Account(Long id, Long studentId, Double balance, LocalDate dateCreated) {
        this.id = id;
        this.studentId = studentId;
        this.balance = balance;
        this.dateCreated = dateCreated;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Double getBalance() {
        return balance;
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

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }
}

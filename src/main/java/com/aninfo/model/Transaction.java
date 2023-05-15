package com.aninfo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double sum;
    private TransactionType type;

    public Transaction() {
    }

    public Transaction(Double sum, TransactionType type) {
        this.sum = sum;
        this.type = type;
    }

    public Long getId() { return this.id; }

    public Double getSum() { return this.sum; }

    public TransactionType getType() { return this.type; }
}


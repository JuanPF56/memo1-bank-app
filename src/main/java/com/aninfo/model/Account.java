package com.aninfo.model;

import javax.persistence.*;
import java.util.*;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cbu;

    private Double balance;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    public Account(){
    }

    public Account(Double balance) {
        this.balance = balance;
    }

    public Long getCbu() {
        return cbu;
    }

    public void setCbu(Long cbu) {
        this.cbu = cbu;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public void newTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }
    public void deleteTransaction(Long id) {
        this.transactions.removeIf(transaction -> transaction.getId() == id);
    }

    public List<Transaction> getTransactions() {
        return this.transactions;
    }

    public Optional<Transaction> getTransaction(Long id) {
        return this.transactions.stream().filter(transaction -> transaction.getId() == id).findAny();
    }
}

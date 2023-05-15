package com.aninfo.service;

import com.aninfo.exceptions.DepositNegativeSumException;
import com.aninfo.exceptions.InsufficientFundsException;
import com.aninfo.exceptions.InvalidTransactionID;
import com.aninfo.exceptions.InvalidTransactionTypeException;
import com.aninfo.model.Account;
import com.aninfo.model.Transaction;
import com.aninfo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

import static com.aninfo.model.TransactionType.DEPOSIT;
import static com.aninfo.model.TransactionType.WITHDRAWAL;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Collection<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> findById(Long cbu) {
        return accountRepository.findById(cbu);
    }

    public void save(Account account) {
        accountRepository.save(account);
    }

    public void deleteById(Long cbu) {
        accountRepository.deleteById(cbu);
    }

    @Transactional
    public Account withdraw(Long cbu, Double sum) {
        Account account = accountRepository.findAccountByCbu(cbu);

        if (account.getBalance() < sum) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        account.setBalance(account.getBalance() - sum);
        accountRepository.save(account);

        return account;
    }

    @Transactional
    public Account deposit(Long cbu, Double sum) {
        if (sum <= 0) {
            throw new DepositNegativeSumException("Cannot deposit negative sums or 0");
        }

        if (sum >= 2000) {
            Double promoSum = sum * 0.1;
            if (promoSum > 500.0) {
                promoSum = 500.0;
            }
            sum = sum + promoSum;
        }

        Account account = accountRepository.findAccountByCbu(cbu);
        account.setBalance(account.getBalance() + sum);
        accountRepository.save(account);

        return account;
    }

    public Transaction createTransaction(Long cbu, Transaction transaction) {
        Account account;

        if (transaction.getType() == DEPOSIT) {
            account = deposit(cbu, transaction.getSum());
        } else if (transaction.getType() == WITHDRAWAL) {
            if (transaction.getSum() <= 0) {
                throw new InvalidTransactionTypeException("Cannot withdraw negative sums or 0");
            }
            account = withdraw(cbu, transaction.getSum());
        } else throw new InvalidTransactionTypeException("Transaction needs to be DEPOSIT or WITHDRAWAL");

        account.newTransaction(transaction);
        accountRepository.save(account);

        return transaction;
    }

    public Collection<Transaction> getTransactions(Long cbu) {
        Account account = accountRepository.findAccountByCbu(cbu);
        return account.getTransactions();
    }

    public Optional<Transaction> findTransactionById(Long id) {
        return accountRepository.findAll().stream()
                .flatMap(account -> account.getTransaction(id).stream())
                .findFirst();  // Find first occurrence of transaction with that id in all accounts.
    }


    public void deleteTransactionById(Long id) {
        Boolean deleted = false;
        Collection<Account> accounts = accountRepository.findAll();
        for (Account account : accounts) {
            Optional<Transaction> transactionOptional = account.getTransaction(id);
            if (transactionOptional.isPresent()) {
                // Only if we want to revert the transaction before deleting it.
                handleTransactionDeletion(account, transactionOptional.get());
                // -------------------------------------------------------------
                account.deleteTransaction(id);
                accountRepository.save(account);
                deleted = true;
            }
        }
        if (!deleted) {
            throw new InvalidTransactionID("No transaction with id " + id + " exists!");
        }
    }

    private void handleTransactionDeletion(Account account, Transaction transaction) {
        if (transaction.getType() == DEPOSIT) {  // If it was a deposit, withdraw the money (if possible).
            if (account.getBalance() < transaction.getSum()) {
                throw new InsufficientFundsException("Cannot revert transaction: insufficient funds");
            }
            account.setBalance(account.getBalance() - transaction.getSum());
            accountRepository.save(account);
        } else if (transaction.getType() == WITHDRAWAL) {  // If it was a withdrawal, deposit the money.
            account.setBalance(account.getBalance() + transaction.getSum());
            accountRepository.save(account);
        }
    }
}

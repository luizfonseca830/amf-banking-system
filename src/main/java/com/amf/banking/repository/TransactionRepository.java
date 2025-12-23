package com.amf.banking.repository;

import com.amf.banking.model.Account;
import com.amf.banking.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    @Query("{ $or: [ { 'sourceAccount._id': ?0 }, { 'destinationAccount._id': ?0 } ], 'transactionDate': { $gte: ?1, $lte: ?2 } }")
    List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ $or: [ { 'sourceAccount._id': ?0 }, { 'destinationAccount._id': ?0 } ] }")
    List<Transaction> findByAccountId(String accountId);

    // Métodos alternativos usando DBRef diretamente
    List<Transaction> findBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount);

    List<Transaction> findBySourceAccountOrDestinationAccountAndTransactionDateBetween(
        Account sourceAccount,
        Account destinationAccount,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
}

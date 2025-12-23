package com.amf.banking.repository;

import com.amf.banking.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    @Query("{ $or: [ { 'sourceAccount.$id': ?0 }, { 'destinationAccount.$id': ?0 } ], 'transactionDate': { $gte: ?1, $lte: ?2 } }")
    List<Transaction> findByAccountIdAndDateRange(String accountId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("{ $or: [ { 'sourceAccount.$id': ?0 }, { 'destinationAccount.$id': ?0 } ] }")
    List<Transaction> findByAccountId(String accountId);
}

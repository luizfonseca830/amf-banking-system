package com.amf.banking.model;

import com.amf.banking.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {

    @Id
    private String id;

    @DBRef
    private Account sourceAccount;

    @DBRef
    private Account destinationAccount;

    private BigDecimal amount;

    private TransactionType transactionType;

    @CreatedDate
    private LocalDateTime transactionDate;

    private String description;
}

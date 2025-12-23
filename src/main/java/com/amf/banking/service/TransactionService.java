package com.amf.banking.service;

import com.amf.banking.dto.TransactionDTO;
import com.amf.banking.exception.BusinessException;
import com.amf.banking.exception.ResourceNotFoundException;
import com.amf.banking.model.Account;
import com.amf.banking.model.Transaction;
import com.amf.banking.model.enums.TransactionType;
import com.amf.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final ModelMapper modelMapper;

    @Transactional
    public TransactionDTO createTransfer(TransactionDTO transactionDTO) {
        log.info("Processing transfer from account {} to account {}",
                transactionDTO.getSourceAccountId(),
                transactionDTO.getDestinationAccountId());

        validateTransfer(transactionDTO);

        Account sourceAccount = accountService.findAccountById(transactionDTO.getSourceAccountId());
        Account destinationAccount = accountService.findAccountById(transactionDTO.getDestinationAccountId());

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BusinessException("Não é possível transferir para a mesma conta");
        }

        if (sourceAccount.getBalance().compareTo(transactionDTO.getAmount()) < 0) {
            throw new BusinessException("Saldo insuficiente na conta origem");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transactionDTO.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(transactionDTO.getAmount()));

        accountService.saveAccount(sourceAccount);
        accountService.saveAccount(destinationAccount);

        Transaction transaction = Transaction.builder()
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(transactionDTO.getAmount())
                .transactionType(TransactionType.TRANSFERENCIA)
                .description(transactionDTO.getDescription())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transfer completed successfully. Transaction ID: {}", savedTransaction.getId());

        return buildTransactionDTO(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionDTO getTransactionById(String id) {
        log.info("Fetching transaction with ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com ID: " + id));

        return buildTransactionDTO(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAccountTransactions(String accountId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching transactions for account ID: {} between {} and {}", accountId, startDate, endDate);

        accountService.findAccountById(accountId);

        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByAccountId(accountId);
        }

        return transactions.stream()
                .map(this::buildTransactionDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactions() {
        log.info("Fetching all transactions");

        return transactionRepository.findAll().stream()
                .map(this::buildTransactionDTO)
                .collect(Collectors.toList());
    }

    private void validateTransfer(TransactionDTO transactionDTO) {
        if (transactionDTO.getAmount() == null || transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor da transferência deve ser maior que zero");
        }

        if (transactionDTO.getSourceAccountId() == null || transactionDTO.getSourceAccountId().isEmpty()) {
            throw new BusinessException("Conta origem é obrigatória");
        }

        if (transactionDTO.getDestinationAccountId() == null || transactionDTO.getDestinationAccountId().isEmpty()) {
            throw new BusinessException("Conta destino é obrigatória");
        }
    }

    private TransactionDTO buildTransactionDTO(Transaction transaction) {
        TransactionDTO dto = modelMapper.map(transaction, TransactionDTO.class);
        dto.setSourceAccountId(transaction.getSourceAccount().getId());
        dto.setSourceAccountNumber(transaction.getSourceAccount().getAccountNumber());
        dto.setDestinationAccountId(transaction.getDestinationAccount().getId());
        dto.setDestinationAccountNumber(transaction.getDestinationAccount().getAccountNumber());
        return dto;
    }
}

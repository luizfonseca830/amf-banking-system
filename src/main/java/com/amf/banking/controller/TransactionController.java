package com.amf.banking.controller;

import com.amf.banking.dto.TransactionDTO;
import com.amf.banking.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Endpoints para gerenciamento de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Realizar transferência", description = "Realiza uma transferência entre duas contas")
    public ResponseEntity<TransactionDTO> createTransfer(@Valid @RequestBody TransactionDTO transactionDTO) {
        TransactionDTO transaction = transactionService.createTransfer(transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID", description = "Retorna os dados de uma transação específica")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable String id) {
        TransactionDTO transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Consultar extrato", description = "Retorna o extrato de movimentações de uma conta")
    public ResponseEntity<List<TransactionDTO>> getAccountTransactions(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<TransactionDTO> transactions = transactionService.getAccountTransactions(accountId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping
    @Operation(summary = "Listar todas as transações", description = "Retorna a lista de todas as transações")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
}

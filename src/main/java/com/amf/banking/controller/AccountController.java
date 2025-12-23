package com.amf.banking.controller;

import com.amf.banking.dto.AccountDTO;
import com.amf.banking.dto.BalanceDTO;
import com.amf.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Endpoints para gerenciamento de contas bancárias")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Criar nova conta", description = "Cria uma nova conta bancária para um cliente")
    public ResponseEntity<AccountDTO> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
        AccountDTO createdAccount = accountService.createAccount(accountDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID", description = "Retorna os dados de uma conta específica")
    public ResponseEntity<AccountDTO> getAccountById(@PathVariable String id) {
        AccountDTO account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Buscar conta por número", description = "Retorna os dados de uma conta pelo número")
    public ResponseEntity<AccountDTO> getAccountByNumber(@PathVariable String accountNumber) {
        AccountDTO account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar contas por cliente", description = "Retorna todas as contas de um cliente específico")
    public ResponseEntity<List<AccountDTO>> getAccountsByClientId(@PathVariable String clientId) {
        List<AccountDTO> accounts = accountService.getAccountsByClientId(clientId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    @Operation(summary = "Listar todas as contas", description = "Retorna a lista de todas as contas cadastradas")
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<AccountDTO> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Consultar saldo", description = "Retorna o saldo atual de uma conta")
    public ResponseEntity<BalanceDTO> getAccountBalance(@PathVariable String id) {
        BalanceDTO balance = accountService.getAccountBalance(id);
        return ResponseEntity.ok(balance);
    }
}

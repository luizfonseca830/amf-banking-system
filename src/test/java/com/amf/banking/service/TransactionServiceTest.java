package com.amf.banking.service;

import com.amf.banking.dto.TransactionDTO;
import com.amf.banking.exception.BusinessException;
import com.amf.banking.model.Account;
import com.amf.banking.model.Client;
import com.amf.banking.model.Transaction;
import com.amf.banking.model.enums.AccountType;
import com.amf.banking.model.enums.TransactionType;
import com.amf.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account destinationAccount;
    private TransactionDTO transactionDTO;
    private Transaction transaction;
    private Client client;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id("1")
                .fullName("João Silva")
                .cpf("12345678901")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        sourceAccount = Account.builder()
                .id("1")
                .accountNumber("1234567890")
                .client(client)
                .accountType(AccountType.CORRENTE)
                .balance(new BigDecimal("1000.00"))
                .build();

        destinationAccount = Account.builder()
                .id("2")
                .accountNumber("0987654321")
                .client(client)
                .accountType(AccountType.POUPANCA)
                .balance(new BigDecimal("500.00"))
                .build();

        transactionDTO = TransactionDTO.builder()
                .sourceAccountId("1")
                .destinationAccountId("2")
                .amount(new BigDecimal("100.00"))
                .description("Teste de transferência")
                .build();

        transaction = Transaction.builder()
                .id("1")
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFERENCIA)
                .description("Teste de transferência")
                .build();
    }

    @Test
    void createTransfer_Success() {
        when(accountService.findAccountById("1")).thenReturn(sourceAccount);
        when(accountService.findAccountById("2")).thenReturn(destinationAccount);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(modelMapper.map(transaction, TransactionDTO.class)).thenReturn(transactionDTO);

        TransactionDTO result = transactionService.createTransfer(transactionDTO);

        assertNotNull(result);
        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("600.00"), destinationAccount.getBalance());
        verify(accountService, times(1)).saveAccount(sourceAccount);
        verify(accountService, times(1)).saveAccount(destinationAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createTransfer_InsufficientBalance_ThrowsException() {
        transactionDTO.setAmount(new BigDecimal("2000.00"));

        when(accountService.findAccountById("1")).thenReturn(sourceAccount);
        when(accountService.findAccountById("2")).thenReturn(destinationAccount);

        assertThrows(BusinessException.class, () -> transactionService.createTransfer(transactionDTO));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransfer_SameAccount_ThrowsException() {
        transactionDTO.setDestinationAccountId("1");

        when(accountService.findAccountById("1")).thenReturn(sourceAccount);

        assertThrows(BusinessException.class, () -> transactionService.createTransfer(transactionDTO));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createTransfer_InvalidAmount_ThrowsException() {
        transactionDTO.setAmount(BigDecimal.ZERO);

        assertThrows(BusinessException.class, () -> transactionService.createTransfer(transactionDTO));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}

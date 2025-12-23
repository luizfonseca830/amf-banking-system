package com.amf.banking.controller;

import com.amf.banking.dto.TransactionDTO;
import com.amf.banking.exception.BusinessException;
import com.amf.banking.exception.ResourceNotFoundException;
import com.amf.banking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.amf.banking.config.MongoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = TransactionController.class,
    excludeAutoConfiguration = MongoAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class)
)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        transactionDTO = TransactionDTO.builder()
                .id("1")
                .sourceAccountId("account1")
                .sourceAccountNumber("1234567890")
                .destinationAccountId("account2")
                .destinationAccountNumber("0987654321")
                .amount(BigDecimal.valueOf(50.00))
                .description("Transferência teste")
                .transactionDate(LocalDateTime.now())
                .build();
    }

    @Test
    void createTransfer_Success() throws Exception {
        when(transactionService.createTransfer(any(TransactionDTO.class))).thenReturn(transactionDTO);

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.sourceAccountNumber").value("1234567890"))
                .andExpect(jsonPath("$.destinationAccountNumber").value("0987654321"));

        verify(transactionService, times(1)).createTransfer(any(TransactionDTO.class));
    }

    @Test
    void createTransfer_InsufficientBalance() throws Exception {
        when(transactionService.createTransfer(any(TransactionDTO.class)))
                .thenThrow(new BusinessException("Saldo insuficiente na conta origem"));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());

        verify(transactionService, times(1)).createTransfer(any(TransactionDTO.class));
    }

    @Test
    void getTransactionById_Success() throws Exception {
        when(transactionService.getTransactionById("1")).thenReturn(transactionDTO);

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.amount").value(50.00));

        verify(transactionService, times(1)).getTransactionById("1");
    }

    @Test
    void getTransactionById_NotFound() throws Exception {
        when(transactionService.getTransactionById("999"))
                .thenThrow(new ResourceNotFoundException("Transação não encontrada"));

        mockMvc.perform(get("/api/v1/transactions/999"))
                .andExpect(status().isNotFound());

        verify(transactionService, times(1)).getTransactionById("999");
    }

    @Test
    void getAccountTransactions_Success() throws Exception {
        TransactionDTO transaction2 = TransactionDTO.builder()
                .id("2")
                .sourceAccountId("account1")
                .sourceAccountNumber("1234567890")
                .destinationAccountId("account3")
                .destinationAccountNumber("1111111111")
                .amount(BigDecimal.valueOf(30.00))
                .description("Outra transferência")
                .transactionDate(LocalDateTime.now())
                .build();

        List<TransactionDTO> transactions = Arrays.asList(transactionDTO, transaction2);
        when(transactionService.getAccountTransactions(eq("account1"), any(), any()))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions/account/account1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[1].amount").value(30.00));

        verify(transactionService, times(1)).getAccountTransactions(eq("account1"), any(), any());
    }

    @Test
    void getAccountTransactions_WithDateFilter_Success() throws Exception {
        List<TransactionDTO> transactions = Arrays.asList(transactionDTO);
        when(transactionService.getAccountTransactions(eq("account1"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions/account/account1")
                        .param("startDate", "2024-01-01T00:00:00")
                        .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(transactionService, times(1)).getAccountTransactions(eq("account1"), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getAllTransactions_Success() throws Exception {
        List<TransactionDTO> transactions = Arrays.asList(transactionDTO);
        when(transactionService.getAllTransactions()).thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(transactionService, times(1)).getAllTransactions();
    }

    @Test
    void createTransfer_InvalidData_BadRequest() throws Exception {
        TransactionDTO invalidTransaction = TransactionDTO.builder()
                .amount(BigDecimal.valueOf(-10.00))
                .build();

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTransaction)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never()).createTransfer(any(TransactionDTO.class));
    }

    @Test
    void createTransfer_SameAccount_BadRequest() throws Exception {
        when(transactionService.createTransfer(any(TransactionDTO.class)))
                .thenThrow(new IllegalArgumentException("Não é possível transferir para a mesma conta"));

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());

        verify(transactionService, times(1)).createTransfer(any(TransactionDTO.class));
    }
}

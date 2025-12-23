package com.amf.banking.controller;

import com.amf.banking.dto.AccountDTO;
import com.amf.banking.dto.BalanceDTO;
import com.amf.banking.exception.ResourceNotFoundException;
import com.amf.banking.model.enums.AccountType;
import com.amf.banking.service.AccountService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AccountController.class,
    excludeAutoConfiguration = MongoAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class)
)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private AccountDTO accountDTO;

    @BeforeEach
    void setUp() {
        accountDTO = AccountDTO.builder()
                .id("1")
                .accountNumber("1234567890")
                .clientId("client1")
                .clientName("João Silva")
                .accountType(AccountType.CORRENTE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void createAccount_Success() throws Exception {
        when(accountService.createAccount(any(AccountDTO.class))).thenReturn(accountDTO);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.balance").value(100.00));

        verify(accountService, times(1)).createAccount(any(AccountDTO.class));
    }

    @Test
    void getAccountById_Success() throws Exception {
        when(accountService.getAccountById("1")).thenReturn(accountDTO);

        mockMvc.perform(get("/api/v1/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"));

        verify(accountService, times(1)).getAccountById("1");
    }

    @Test
    void getAccountById_NotFound() throws Exception {
        when(accountService.getAccountById("999")).thenThrow(new ResourceNotFoundException("Conta não encontrada"));

        mockMvc.perform(get("/api/v1/accounts/999"))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccountById("999");
    }

    @Test
    void getAccountByNumber_Success() throws Exception {
        when(accountService.getAccountByNumber("1234567890")).thenReturn(accountDTO);

        mockMvc.perform(get("/api/v1/accounts/number/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.clientName").value("João Silva"));

        verify(accountService, times(1)).getAccountByNumber("1234567890");
    }

    @Test
    void getAccountsByClientId_Success() throws Exception {
        AccountDTO account2 = AccountDTO.builder()
                .id("2")
                .accountNumber("0987654321")
                .clientId("client1")
                .clientName("João Silva")
                .accountType(AccountType.POUPANCA)
                .balance(BigDecimal.valueOf(200.00))
                .build();

        List<AccountDTO> accounts = Arrays.asList(accountDTO, account2);
        when(accountService.getAccountsByClientId("client1")).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/accounts/client/client1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountType").value("CORRENTE"))
                .andExpect(jsonPath("$[1].accountType").value("POUPANCA"));

        verify(accountService, times(1)).getAccountsByClientId("client1");
    }

    @Test
    void getAllAccounts_Success() throws Exception {
        List<AccountDTO> accounts = Arrays.asList(accountDTO);
        when(accountService.getAllAccounts()).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void getAccountBalance_Success() throws Exception {
        BalanceDTO balanceDTO = BalanceDTO.builder()
                .accountNumber("1234567890")
                .balance(BigDecimal.valueOf(100.00))
                .build();

        when(accountService.getAccountBalance("1")).thenReturn(balanceDTO);

        mockMvc.perform(get("/api/v1/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.balance").value(100.00));

        verify(accountService, times(1)).getAccountBalance("1");
    }

    @Test
    void createAccount_InvalidData_BadRequest() throws Exception {
        AccountDTO invalidAccount = AccountDTO.builder()
                .accountType(null)
                .build();

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAccount)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).createAccount(any(AccountDTO.class));
    }
}

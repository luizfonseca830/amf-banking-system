package com.amf.banking.service;

import com.amf.banking.dto.AccountDTO;
import com.amf.banking.dto.BalanceDTO;
import com.amf.banking.exception.ResourceNotFoundException;
import com.amf.banking.model.Account;
import com.amf.banking.model.Client;
import com.amf.banking.repository.AccountRepository;
import com.amf.banking.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public AccountDTO createAccount(AccountDTO accountDTO) {
        log.info("Creating new account for client ID: {}", accountDTO.getClientId());

        Client client = clientRepository.findById(accountDTO.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + accountDTO.getClientId()));

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .client(client)
                .accountType(accountDTO.getAccountType())
                .balance(BigDecimal.valueOf(100.00))
                .build();

        Account savedAccount = accountRepository.save(account);

        log.info("Account created successfully with number: {}", savedAccount.getAccountNumber());

        AccountDTO responseDTO = modelMapper.map(savedAccount, AccountDTO.class);
        responseDTO.setClientId(client.getId());
        responseDTO.setClientName(client.getFullName());

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public AccountDTO getAccountById(String id) {
        log.info("Fetching account with ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com ID: " + id));

        AccountDTO accountDTO = modelMapper.map(account, AccountDTO.class);
        accountDTO.setClientId(account.getClient().getId());
        accountDTO.setClientName(account.getClient().getFullName());

        return accountDTO;
    }

    @Transactional(readOnly = true)
    public AccountDTO getAccountByNumber(String accountNumber) {
        log.info("Fetching account with number: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com número: " + accountNumber));

        AccountDTO accountDTO = modelMapper.map(account, AccountDTO.class);
        accountDTO.setClientId(account.getClient().getId());
        accountDTO.setClientName(account.getClient().getFullName());

        return accountDTO;
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAccountsByClientId(String clientId) {
        log.info("Fetching accounts for client ID: {}", clientId);

        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente não encontrado com ID: " + clientId);
        }

        return accountRepository.findByClientId(clientId).stream()
                .map(account -> {
                    AccountDTO dto = modelMapper.map(account, AccountDTO.class);
                    dto.setClientId(account.getClient().getId());
                    dto.setClientName(account.getClient().getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountDTO> getAllAccounts() {
        log.info("Fetching all accounts");

        return accountRepository.findAll().stream()
                .map(account -> {
                    AccountDTO dto = modelMapper.map(account, AccountDTO.class);
                    dto.setClientId(account.getClient().getId());
                    dto.setClientName(account.getClient().getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BalanceDTO getAccountBalance(String id) {
        log.info("Fetching balance for account ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com ID: " + id));

        return BalanceDTO.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .build();
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.format("%010d", new Random().nextInt(1000000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    public Account findAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com ID: " + id));
    }

    @Transactional
    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
}

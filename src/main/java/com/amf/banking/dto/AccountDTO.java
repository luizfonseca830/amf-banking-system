package com.amf.banking.dto;

import com.amf.banking.model.enums.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountDTO {

    private String id;

    private String accountNumber;

    @NotBlank(message = "ID do cliente é obrigatório")
    private String clientId;

    private String clientName;

    @NotNull(message = "Tipo de conta é obrigatório")
    private AccountType accountType;

    private BigDecimal balance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

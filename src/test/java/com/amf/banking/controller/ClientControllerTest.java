package com.amf.banking.controller;

import com.amf.banking.dto.ClientDTO;
import com.amf.banking.exception.ResourceNotFoundException;
import com.amf.banking.service.ClientService;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ClientController.class,
    excludeAutoConfiguration = MongoAutoConfiguration.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoConfig.class)
)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    private ClientDTO clientDTO;

    @BeforeEach
    void setUp() {
        clientDTO = ClientDTO.builder()
                .id("1")
                .fullName("João Silva")
                .cpf("12345678901")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createClient_Success() throws Exception {
        when(clientService.createClient(any(ClientDTO.class))).thenReturn(clientDTO);

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.fullName").value("João Silva"))
                .andExpect(jsonPath("$.cpf").value("12345678901"));

        verify(clientService, times(1)).createClient(any(ClientDTO.class));
    }

    @Test
    void getClientById_Success() throws Exception {
        when(clientService.getClientById("1")).thenReturn(clientDTO);

        mockMvc.perform(get("/api/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.fullName").value("João Silva"));

        verify(clientService, times(1)).getClientById("1");
    }

    @Test
    void getClientById_NotFound() throws Exception {
        when(clientService.getClientById("999")).thenThrow(new ResourceNotFoundException("Cliente não encontrado"));

        mockMvc.perform(get("/api/v1/clients/999"))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClientById("999");
    }

    @Test
    void getClientByCpf_Success() throws Exception {
        when(clientService.getClientByCpf("12345678901")).thenReturn(clientDTO);

        mockMvc.perform(get("/api/v1/clients/cpf/12345678901"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("12345678901"))
                .andExpect(jsonPath("$.fullName").value("João Silva"));

        verify(clientService, times(1)).getClientByCpf("12345678901");
    }

    @Test
    void getAllClients_Success() throws Exception {
        ClientDTO client2 = ClientDTO.builder()
                .id("2")
                .fullName("Maria Santos")
                .cpf("98765432109")
                .birthDate(LocalDate.of(1985, 5, 15))
                .build();

        List<ClientDTO> clients = Arrays.asList(clientDTO, client2);
        when(clientService.getAllClients()).thenReturn(clients);

        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("João Silva"))
                .andExpect(jsonPath("$[1].fullName").value("Maria Santos"));

        verify(clientService, times(1)).getAllClients();
    }

    @Test
    void updateClient_Success() throws Exception {
        ClientDTO updatedClient = ClientDTO.builder()
                .id("1")
                .fullName("João Silva Atualizado")
                .cpf("12345678901")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        when(clientService.updateClient(eq("1"), any(ClientDTO.class))).thenReturn(updatedClient);

        mockMvc.perform(put("/api/v1/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("João Silva Atualizado"));

        verify(clientService, times(1)).updateClient(eq("1"), any(ClientDTO.class));
    }

    @Test
    void deleteClient_Success() throws Exception {
        doNothing().when(clientService).deleteClient("1");

        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient("1");
    }

    @Test
    void createClient_InvalidData_BadRequest() throws Exception {
        ClientDTO invalidClient = ClientDTO.builder()
                .fullName("")
                .cpf("123")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidClient)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any(ClientDTO.class));
    }
}

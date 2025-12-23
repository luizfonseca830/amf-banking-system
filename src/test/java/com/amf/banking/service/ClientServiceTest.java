package com.amf.banking.service;

import com.amf.banking.dto.ClientDTO;
import com.amf.banking.exception.BusinessException;
import com.amf.banking.exception.ResourceNotFoundException;
import com.amf.banking.model.Client;
import com.amf.banking.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientService clientService;

    private ClientDTO clientDTO;
    private Client client;

    @BeforeEach
    void setUp() {
        clientDTO = ClientDTO.builder()
                .fullName("João Silva")
                .cpf("12345678901")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        client = Client.builder()
                .id("1")
                .fullName("João Silva")
                .cpf("12345678901")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void createClient_Success() {
        when(clientRepository.existsByCpf(anyString())).thenReturn(false);
        when(modelMapper.map(clientDTO, Client.class)).thenReturn(client);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

        ClientDTO result = clientService.createClient(clientDTO);

        assertNotNull(result);
        assertEquals(clientDTO.getFullName(), result.getFullName());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void createClient_CpfAlreadyExists_ThrowsException() {
        when(clientRepository.existsByCpf(anyString())).thenReturn(true);

        assertThrows(BusinessException.class, () -> clientService.createClient(clientDTO));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void getClientById_Success() {
        when(clientRepository.findById(anyString())).thenReturn(Optional.of(client));
        when(modelMapper.map(client, ClientDTO.class)).thenReturn(clientDTO);

        ClientDTO result = clientService.getClientById("1");

        assertNotNull(result);
        assertEquals(clientDTO.getFullName(), result.getFullName());
    }

    @Test
    void getClientById_NotFound_ThrowsException() {
        when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.getClientById("1"));
    }

    @Test
    void deleteClient_Success() {
        when(clientRepository.existsById(anyString())).thenReturn(true);

        clientService.deleteClient("1");

        verify(clientRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteClient_NotFound_ThrowsException() {
        when(clientRepository.existsById(anyString())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> clientService.deleteClient("1"));
        verify(clientRepository, never()).deleteById(anyString());
    }
}

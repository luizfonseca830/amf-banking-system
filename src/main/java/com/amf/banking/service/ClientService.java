package com.amf.banking.service;

import com.amf.banking.dto.ClientDTO;
import com.amf.banking.exception.BusinessException;
import com.amf.banking.exception.ResourceNotFoundException;
import com.amf.banking.model.Client;
import com.amf.banking.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public ClientDTO createClient(ClientDTO clientDTO) {
        log.info("Creating new client with CPF: {}", clientDTO.getCpf());

        if (clientRepository.existsByCpf(clientDTO.getCpf())) {
            throw new BusinessException("CPF já cadastrado no sistema");
        }

        Client client = modelMapper.map(clientDTO, Client.class);
        Client savedClient = clientRepository.save(client);

        log.info("Client created successfully with ID: {}", savedClient.getId());
        return modelMapper.map(savedClient, ClientDTO.class);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientById(String id) {
        log.info("Fetching client with ID: {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        return modelMapper.map(client, ClientDTO.class);
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientByCpf(String cpf) {
        log.info("Fetching client with CPF: {}", cpf);

        Client client = clientRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com CPF: " + cpf));

        return modelMapper.map(client, ClientDTO.class);
    }

    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients() {
        log.info("Fetching all clients");

        return clientRepository.findAll().stream()
                .map(client -> modelMapper.map(client, ClientDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientDTO updateClient(String id, ClientDTO clientDTO) {
        log.info("Updating client with ID: {}", id);

        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + id));

        if (!existingClient.getCpf().equals(clientDTO.getCpf()) &&
                clientRepository.existsByCpf(clientDTO.getCpf())) {
            throw new BusinessException("CPF já cadastrado no sistema");
        }

        existingClient.setFullName(clientDTO.getFullName());
        existingClient.setCpf(clientDTO.getCpf());
        existingClient.setBirthDate(clientDTO.getBirthDate());

        Client updatedClient = clientRepository.save(existingClient);

        log.info("Client updated successfully with ID: {}", updatedClient.getId());
        return modelMapper.map(updatedClient, ClientDTO.class);
    }

    @Transactional
    public void deleteClient(String id) {
        log.info("Deleting client with ID: {}", id);

        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente não encontrado com ID: " + id);
        }

        clientRepository.deleteById(id);
        log.info("Client deleted successfully with ID: {}", id);
    }
}

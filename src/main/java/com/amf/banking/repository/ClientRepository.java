package com.amf.banking.repository;

import com.amf.banking.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {
    Optional<Client> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}

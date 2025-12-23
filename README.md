# Sistema Bancário - AMF Promotora

## Como Executar o Projeto Localmente

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- Docker e Docker Compose (para MongoDB)

### Passo a Passo

#### 1. Iniciar o MongoDB com Docker

```bash
docker-compose up -d
```

O MongoDB será iniciado na porta 27017.

#### 2. Compilar o projeto

```bash
mvn clean install
```

#### 3. Executar a aplicação

Opção 1 - Via Maven:
```bash
mvn spring-boot:run
```

Opção 2 - Via JAR:
```bash
java -jar target/banking-system-1.0.0.jar
```

#### 4. Acessar a aplicação

- **API REST**: http://localhost:8080/api/v1
- **Swagger UI (Documentação da API)**: http://localhost:8080/api/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api-docs
- **Vaadin UI (Interface Web)**: http://localhost:8080

#### Parar a aplicação

Para parar o MongoDB:
```bash
docker-compose down
```

Para parar e remover os dados:
```bash
docker-compose down -v
```

## Documentação da API

### Swagger UI

Após iniciar a aplicação, acesse a documentação interativa da API:

```
http://localhost:8080/api/swagger-ui.html
```

**Nota**: O Swagger está configurado para não conflitar com as rotas do Vaadin UI. Certifique-se de usar a URL correta acima.

### Endpoints Disponíveis

#### Clientes
- `POST /api/v1/clients` - Criar novo cliente
- `GET /api/v1/clients` - Listar todos os clientes
- `GET /api/v1/clients/{id}` - Buscar cliente por ID
- `GET /api/v1/clients/cpf/{cpf}` - Buscar cliente por CPF
- `PUT /api/v1/clients/{id}` - Atualizar cliente
- `DELETE /api/v1/clients/{id}` - Deletar cliente

#### Contas Bancárias
- `POST /api/v1/accounts` - Criar nova conta
- `GET /api/v1/accounts` - Listar todas as contas
- `GET /api/v1/accounts/{id}` - Buscar conta por ID
- `GET /api/v1/accounts/number/{accountNumber}` - Buscar conta por número
- `GET /api/v1/accounts/client/{clientId}` - Listar contas por cliente
- `GET /api/v1/accounts/{id}/balance` - Consultar saldo

#### Transações
- `POST /api/v1/transactions` - Realizar transferência
- `GET /api/v1/transactions` - Listar todas as transações
- `GET /api/v1/transactions/{id}` - Buscar transação por ID
- `GET /api/v1/transactions/account/{accountId}` - Consultar extrato

## Instruções para Rodar o Front-end (Vaadin)

O front-end Vaadin é iniciado automaticamente junto com a aplicação Spring Boot.

### Acessar a Interface Web

Após executar a aplicação, acesse:
```
http://localhost:8080
```

### Páginas Disponíveis

| Funcionalidade | URL | Descrição |
|----------------|-----|-----------|
| **Home** | `http://localhost:8080/` | Página inicial (Cadastro de Clientes) |
| **Contas** | `http://localhost:8080/accounts` | Criação e listagem de contas bancárias |
| **Saldo** | `http://localhost:8080/balance` | Consulta de saldo em tempo real |
| **Transferências** | `http://localhost:8080/transfer` | Realizar transferências entre contas |
| **Extrato** | `http://localhost:8080/statement` | Consultar extrato com filtros por período |

### Funcionalidades do Frontend

1. **Cadastro de Clientes** (Página Inicial)
   - Formulário para cadastrar novos clientes
   - Validação de CPF e data de nascimento
   - Grid com listagem de clientes

2. **Criação de Contas**
   - Formulário para criar contas bancárias
   - Seleção de cliente e tipo de conta (Corrente/Poupança)
   - Grid com listagem de contas

3. **Consulta de Saldo**
   - Seleção de conta via dropdown
   - Exibição do saldo atual em tempo real

4. **Transferências**
   - Seleção de conta origem e destino
   - Campo para valor e descrição
   - Validação de saldo disponível
   - Confirmação de transferência realizada

5. **Extrato de Movimentações**
   - Seleção de conta para consulta
   - Filtros por período (data inicial e final)
   - Grid com histórico completo de transações (envio e recebimento)

### Navegação

O sistema possui um menu lateral com acesso a todas as funcionalidades.

## Instruções de Testes

### Testes Unitários

Para executar apenas os testes unitários:

```bash
mvn test
```

### Testes de Integração

Para executar os testes de integração:

```bash
mvn verify
```

Os testes de integração utilizam **TestContainers** para criar um container MongoDB temporário automaticamente.

### Executar Todos os Testes

Para executar unitários e de integração:

```bash
mvn clean verify
```

### Relatório de Cobertura

Para gerar relatório de cobertura de testes (se configurado):

```bash
mvn clean test jacoco:report
```

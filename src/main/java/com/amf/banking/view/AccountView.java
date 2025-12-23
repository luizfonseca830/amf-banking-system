package com.amf.banking.view;

import com.amf.banking.dto.AccountDTO;
import com.amf.banking.dto.ClientDTO;
import com.amf.banking.model.enums.AccountType;
import com.amf.banking.service.AccountService;
import com.amf.banking.service.ClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "accounts", layout = MainLayout.class)
@PageTitle("Contas | AMF Banking")
public class AccountView extends VerticalLayout {

    private final AccountService accountService;
    private final ClientService clientService;
    private final Grid<AccountDTO> grid = new Grid<>(AccountDTO.class, false);
    private final Binder<AccountDTO> binder = new Binder<>(AccountDTO.class);

    private ComboBox<ClientDTO> clientCombo = new ComboBox<>("Cliente");
    private ComboBox<AccountType> accountTypeCombo = new ComboBox<>("Tipo de Conta");

    private Button saveButton = new Button("Criar Conta");
    private Button clearButton = new Button("Limpar");

    public AccountView(AccountService accountService, ClientService clientService) {
        this.accountService = accountService;
        this.clientService = clientService;

        setSizeFull();
        setPadding(true);

        configureGrid();
        configureForm();

        add(getFormLayout(), grid);
        refreshGrid();
        loadClients();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(AccountDTO::getAccountNumber).setHeader("Número da Conta").setSortable(true);
        grid.addColumn(AccountDTO::getClientName).setHeader("Cliente").setSortable(true);
        grid.addColumn(account -> account.getAccountType() == AccountType.CORRENTE ? "Conta Corrente" : "Poupança")
                .setHeader("Tipo").setSortable(true);
        grid.addColumn(account -> String.format("R$ %.2f", account.getBalance())).setHeader("Saldo").setSortable(true);
        grid.addColumn(account -> {
            if (account.getCreatedAt() != null) {
                return account.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return "";
        }).setHeader("Criado em").setSortable(true);
    }

    private void configureForm() {
        clientCombo.setItemLabelGenerator(ClientDTO::getFullName);
        clientCombo.setRequired(true);

        accountTypeCombo.setItems(AccountType.values());
        accountTypeCombo.setItemLabelGenerator(type ->
            type == AccountType.CORRENTE ? "Conta Corrente" : "Poupança"
        );
        accountTypeCombo.setRequired(true);

        binder.forField(clientCombo)
                .asRequired("Cliente é obrigatório")
                .bind(
                    account -> clientCombo.getOptionalValue().orElse(null),
                    (account, client) -> account.setClientId(client != null ? client.getId() : null)
                );

        binder.forField(accountTypeCombo)
                .asRequired("Tipo de conta é obrigatório")
                .bind(AccountDTO::getAccountType, AccountDTO::setAccountType);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveAccount());

        clearButton.addClickListener(e -> clearForm());
    }

    private VerticalLayout getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(clientCombo, accountTypeCombo);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        HorizontalLayout buttons = new HorizontalLayout(saveButton, clearButton);
        VerticalLayout form = new VerticalLayout(formLayout, buttons);
        form.setPadding(false);

        return form;
    }

    private void saveAccount() {
        try {
            AccountDTO accountDTO = new AccountDTO();
            if (binder.writeBeanIfValid(accountDTO)) {
                accountService.createAccount(accountDTO);
                showNotification("Conta criada com sucesso!", NotificationVariant.LUMO_SUCCESS);
                clearForm();
                refreshGrid();
            } else {
                showNotification("Preencha todos os campos corretamente", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            showNotification("Erro ao criar conta: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        binder.readBean(new AccountDTO());
        clientCombo.clear();
        accountTypeCombo.clear();
    }

    private void refreshGrid() {
        try {
            grid.setItems(accountService.getAllAccounts());
        } catch (Exception e) {
            showNotification("Erro ao carregar contas: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadClients() {
        try {
            List<ClientDTO> clients = clientService.getAllClients();
            clientCombo.setItems(clients);
        } catch (Exception e) {
            showNotification("Erro ao carregar clientes: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}

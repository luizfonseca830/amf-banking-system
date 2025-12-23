package com.amf.banking.view;

import com.amf.banking.dto.AccountDTO;
import com.amf.banking.dto.TransactionDTO;
import com.amf.banking.service.AccountService;
import com.amf.banking.service.TransactionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.List;

@Route(value = "statement", layout = MainLayout.class)
@PageTitle("Extrato | AMF Banking")
public class StatementView extends VerticalLayout {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final Grid<TransactionDTO> grid = new Grid<>(TransactionDTO.class, false);

    private ComboBox<AccountDTO> accountCombo = new ComboBox<>("Selecione a Conta");
    private DateTimePicker startDatePicker = new DateTimePicker("Data Inicial");
    private DateTimePicker endDatePicker = new DateTimePicker("Data Final");
    private Button searchButton = new Button("Consultar");
    private Button clearButton = new Button("Limpar Filtros");

    public StatementView(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureGrid();
        configureForm();

        H3 title = new H3("Extrato de Movimentações");
        add(title, getFormLayout(), grid);

        loadAccounts();
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(transaction -> {
            if (transaction.getTransactionDate() != null) {
                return transaction.getTransactionDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                );
            }
            return "";
        }).setHeader("Data/Hora").setSortable(true);

        grid.addColumn(TransactionDTO::getSourceAccountNumber)
                .setHeader("Conta Origem")
                .setSortable(true);

        grid.addColumn(TransactionDTO::getDestinationAccountNumber)
                .setHeader("Conta Destino")
                .setSortable(true);

        grid.addColumn(transaction -> String.format("R$ %.2f", transaction.getAmount()))
                .setHeader("Valor")
                .setSortable(true);

        grid.addColumn(transaction -> "Transferência")
                .setHeader("Tipo")
                .setSortable(true);

        grid.addColumn(TransactionDTO::getDescription)
                .setHeader("Descrição")
                .setSortable(true);
    }

    private void configureForm() {
        accountCombo.setItemLabelGenerator(account ->
                String.format("%s - %s",
                        account.getAccountNumber(),
                        account.getClientName())
        );
        accountCombo.setRequired(true);
        accountCombo.setWidthFull();

        // Configurar DateTimePickers para português
        startDatePicker.setLocale(new java.util.Locale("pt", "BR"));
        startDatePicker.setWidthFull();

        DatePicker.DatePickerI18n i18n = new DatePicker.DatePickerI18n();
        i18n.setMonthNames(java.util.Arrays.asList(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        ));
        i18n.setWeekdays(java.util.Arrays.asList(
            "Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"
        ));
        i18n.setWeekdaysShort(java.util.Arrays.asList(
            "Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"
        ));
        i18n.setToday("Hoje");
        i18n.setCancel("Cancelar");
        startDatePicker.setDatePickerI18n(i18n);

        endDatePicker.setLocale(new java.util.Locale("pt", "BR"));
        endDatePicker.setWidthFull();
        endDatePicker.setDatePickerI18n(i18n);

        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        searchButton.addClickListener(e -> loadTransactions());

        clearButton.addClickListener(e -> clearFilters());
    }

    private VerticalLayout getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(accountCombo, startDatePicker, endDatePicker);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 3)
        );
        formLayout.setMaxWidth("900px");

        HorizontalLayout buttons = new HorizontalLayout(searchButton, clearButton);

        VerticalLayout form = new VerticalLayout(formLayout, buttons);
        form.setPadding(false);
        form.setMaxWidth("900px");

        return form;
    }

    private void loadTransactions() {
        AccountDTO selectedAccount = accountCombo.getValue();

        if (selectedAccount == null) {
            showNotification("Selecione uma conta", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            LocalDateTime startDate = startDatePicker.getValue();
            LocalDateTime endDate = endDatePicker.getValue();

            List<TransactionDTO> transactions = transactionService.getAccountTransactions(
                    selectedAccount.getId(),
                    startDate,
                    endDate
            );

            grid.setItems(transactions);

            if (transactions.isEmpty()) {
                showNotification("Nenhuma transação encontrada para o período selecionado",
                        NotificationVariant.LUMO_CONTRAST);
            } else {
                showNotification(
                        String.format("Foram encontradas %d transação(ões)", transactions.size()),
                        NotificationVariant.LUMO_SUCCESS
                );
            }
        } catch (Exception e) {
            showNotification("Erro ao carregar transações: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearFilters() {
        accountCombo.clear();
        startDatePicker.clear();
        endDatePicker.clear();
        grid.setItems();
    }

    private void loadAccounts() {
        try {
            List<AccountDTO> accounts = accountService.getAllAccounts();
            accountCombo.setItems(accounts);
        } catch (Exception e) {
            showNotification("Erro ao carregar contas: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        notification.open();
    }
}

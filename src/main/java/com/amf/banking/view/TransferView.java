package com.amf.banking.view;

import com.amf.banking.dto.AccountDTO;
import com.amf.banking.dto.TransactionDTO;
import com.amf.banking.service.AccountService;
import com.amf.banking.service.TransactionService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "transfer", layout = MainLayout.class)
@PageTitle("Transferências | AMF Banking")
public class TransferView extends VerticalLayout {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final Binder<TransactionDTO> binder = new Binder<>(TransactionDTO.class);

    private ComboBox<AccountDTO> sourceAccountCombo = new ComboBox<>("Conta Origem");
    private ComboBox<AccountDTO> destinationAccountCombo = new ComboBox<>("Conta Destino");
    private BigDecimalField amountField = new BigDecimalField("Valor");
    private TextArea descriptionField = new TextArea("Descrição");

    private Button transferButton = new Button("Realizar Transferência");
    private Button clearButton = new Button("Limpar");

    public TransferView(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureForm();

        H3 title = new H3("Realizar Transferência");
        add(title, getFormLayout());

        loadAccounts();
    }

    private void configureForm() {
        sourceAccountCombo.setItemLabelGenerator(account ->
                String.format("%s - %s (R$ %.2f)",
                        account.getAccountNumber(),
                        account.getClientName(),
                        account.getBalance())
        );
        sourceAccountCombo.setRequired(true);
        sourceAccountCombo.setWidthFull();

        destinationAccountCombo.setItemLabelGenerator(account ->
                String.format("%s - %s",
                        account.getAccountNumber(),
                        account.getClientName())
        );
        destinationAccountCombo.setRequired(true);
        destinationAccountCombo.setWidthFull();

        amountField.setPrefixComponent(new com.vaadin.flow.component.html.Span("R$"));
        amountField.setRequiredIndicatorVisible(true);
        amountField.setWidthFull();
        amountField.setPlaceholder("0.01");

        descriptionField.setMaxLength(200);
        descriptionField.setWidthFull();

        binder.forField(sourceAccountCombo)
                .asRequired("Conta origem é obrigatória")
                .bind(
                    transaction -> sourceAccountCombo.getOptionalValue().orElse(null),
                    (transaction, account) -> transaction.setSourceAccountId(account != null ? account.getId() : null)
                );

        binder.forField(destinationAccountCombo)
                .asRequired("Conta destino é obrigatória")
                .bind(
                    transaction -> destinationAccountCombo.getOptionalValue().orElse(null),
                    (transaction, account) -> transaction.setDestinationAccountId(account != null ? account.getId() : null)
                );

        binder.forField(amountField)
                .asRequired("Valor é obrigatório")
                .withValidator(value -> value != null && value.compareTo(BigDecimal.ZERO) > 0,
                        "Valor deve ser maior que zero")
                .bind(TransactionDTO::getAmount, TransactionDTO::setAmount);

        binder.forField(descriptionField)
                .bind(TransactionDTO::getDescription, TransactionDTO::setDescription);

        transferButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        transferButton.addClickListener(e -> performTransfer());

        clearButton.addClickListener(e -> clearForm());
    }

    private VerticalLayout getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(sourceAccountCombo, destinationAccountCombo, amountField, descriptionField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        formLayout.setMaxWidth("800px");

        com.vaadin.flow.component.orderedlayout.HorizontalLayout buttons =
                new com.vaadin.flow.component.orderedlayout.HorizontalLayout(transferButton, clearButton);

        VerticalLayout form = new VerticalLayout(formLayout, buttons);
        form.setPadding(false);
        form.setMaxWidth("800px");

        return form;
    }

    private void performTransfer() {
        try {
            TransactionDTO transactionDTO = new TransactionDTO();
            if (binder.writeBeanIfValid(transactionDTO)) {
                transactionService.createTransfer(transactionDTO);
                showNotification("Transferência realizada com sucesso!", NotificationVariant.LUMO_SUCCESS);
                clearForm();
                loadAccounts();
            } else {
                showNotification("Preencha todos os campos corretamente", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            showNotification("Erro ao realizar transferência: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void clearForm() {
        binder.readBean(new TransactionDTO());
        sourceAccountCombo.clear();
        destinationAccountCombo.clear();
        amountField.clear();
        descriptionField.clear();
    }

    private void loadAccounts() {
        try {
            List<AccountDTO> accounts = accountService.getAllAccounts();
            sourceAccountCombo.setItems(accounts);
            destinationAccountCombo.setItems(accounts);
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

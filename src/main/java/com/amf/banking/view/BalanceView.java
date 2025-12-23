package com.amf.banking.view;

import com.amf.banking.dto.AccountDTO;
import com.amf.banking.dto.BalanceDTO;
import com.amf.banking.service.AccountService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "balance", layout = MainLayout.class)
@PageTitle("Consultar Saldo | AMF Banking")
public class BalanceView extends VerticalLayout {

    private final AccountService accountService;

    private ComboBox<AccountDTO> accountCombo = new ComboBox<>("Selecione a Conta");
    private Button consultButton = new Button("Consultar Saldo");
    private H2 balanceLabel = new H2();

    public BalanceView(AccountService accountService) {
        this.accountService = accountService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        configureForm();

        H3 title = new H3("Consulta de Saldo");
        add(title, getFormLayout(), balanceLabel);

        loadAccounts();
    }

    private void configureForm() {
        accountCombo.setItemLabelGenerator(account ->
                String.format("%s - %s (R$ %.2f)",
                        account.getAccountNumber(),
                        account.getClientName(),
                        account.getBalance())
        );
        accountCombo.setRequired(true);
        accountCombo.setWidthFull();

        consultButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        consultButton.addClickListener(e -> consultBalance());

        balanceLabel.setVisible(false);
    }

    private VerticalLayout getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(accountCombo);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );
        formLayout.setMaxWidth("600px");

        com.vaadin.flow.component.orderedlayout.HorizontalLayout buttons =
                new com.vaadin.flow.component.orderedlayout.HorizontalLayout(consultButton);

        VerticalLayout form = new VerticalLayout(formLayout, buttons);
        form.setPadding(false);
        form.setMaxWidth("600px");

        return form;
    }

    private void consultBalance() {
        AccountDTO selectedAccount = accountCombo.getValue();

        if (selectedAccount == null) {
            showNotification("Selecione uma conta", NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            BalanceDTO balance = accountService.getAccountBalance(selectedAccount.getId());
            balanceLabel.setText(String.format("Saldo atual: R$ %.2f", balance.getBalance()));
            balanceLabel.setVisible(true);
        } catch (Exception e) {
            showNotification("Erro ao consultar saldo: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
            balanceLabel.setVisible(false);
        }
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

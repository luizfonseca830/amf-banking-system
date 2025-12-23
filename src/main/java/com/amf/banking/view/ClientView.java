package com.amf.banking.view;

import com.amf.banking.dto.ClientDTO;
import com.amf.banking.service.ClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Clientes | AMF Banking")
public class ClientView extends VerticalLayout {

    private final ClientService clientService;
    private final Grid<ClientDTO> grid = new Grid<>(ClientDTO.class, false);
    private final Binder<ClientDTO> binder = new Binder<>(ClientDTO.class);

    private TextField fullName = new TextField("Nome Completo");
    private TextField cpf = new TextField("CPF");
    private DatePicker birthDate = new DatePicker("Data de Nascimento");

    private Button saveButton = new Button("Salvar");
    private Button clearButton = new Button("Limpar");

    public ClientView(ClientService clientService) {
        this.clientService = clientService;

        setSizeFull();
        setPadding(true);

        configureGrid();
        configureForm();

        add(getFormLayout(), grid);
        refreshGrid();
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(ClientDTO::getFullName).setHeader("Nome Completo").setSortable(true);
        grid.addColumn(ClientDTO::getCpf).setHeader("CPF").setSortable(true);
        grid.addColumn(client -> {
            if (client.getBirthDate() != null) {
                return client.getBirthDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return "";
        }).setHeader("Data de Nascimento").setSortable(true);
        grid.addColumn(client -> {
            if (client.getCreatedAt() != null) {
                return client.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return "";
        }).setHeader("Criado em").setSortable(true);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                populateForm(event.getValue());
            }
        });
    }

    private void configureForm() {
        // Configurar DatePicker para português
        birthDate.setLocale(new java.util.Locale("pt", "BR"));
        birthDate.setPlaceholder("dd/MM/yyyy");

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
        birthDate.setI18n(i18n);

        binder.forField(fullName)
                .asRequired("Nome é obrigatório")
                .bind(ClientDTO::getFullName, ClientDTO::setFullName);

        binder.forField(cpf)
                .asRequired("CPF é obrigatório")
                .withValidator(value -> value.matches("\\d{11}"), "CPF deve conter 11 dígitos")
                .bind(ClientDTO::getCpf, ClientDTO::setCpf);

        binder.forField(birthDate)
                .asRequired("Data de nascimento é obrigatória")
                .withValidator(date -> date.isBefore(LocalDate.now()), "Data deve ser no passado")
                .bind(ClientDTO::getBirthDate, ClientDTO::setBirthDate);

        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(e -> saveClient());

        clearButton.addClickListener(e -> clearForm());
    }

    private VerticalLayout getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(fullName, cpf, birthDate);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 3)
        );

        HorizontalLayout buttons = new HorizontalLayout(saveButton, clearButton);
        VerticalLayout form = new VerticalLayout(formLayout, buttons);
        form.setPadding(false);

        return form;
    }

    private void saveClient() {
        try {
            ClientDTO clientDTO = new ClientDTO();
            if (binder.writeBeanIfValid(clientDTO)) {
                clientService.createClient(clientDTO);
                showNotification("Cliente salvo com sucesso!", NotificationVariant.LUMO_SUCCESS);
                clearForm();
                refreshGrid();
            } else {
                showNotification("Preencha todos os campos corretamente", NotificationVariant.LUMO_ERROR);
            }
        } catch (Exception e) {
            showNotification("Erro ao salvar cliente: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void populateForm(ClientDTO client) {
        binder.readBean(client);
    }

    private void clearForm() {
        binder.readBean(new ClientDTO());
        fullName.clear();
        cpf.clear();
        birthDate.clear();
    }

    private void refreshGrid() {
        try {
            grid.setItems(clientService.getAllClients());
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

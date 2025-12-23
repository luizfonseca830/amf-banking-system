package com.amf.banking.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("AMF Banking System");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM
        );

        DrawerToggle toggle = new DrawerToggle();

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink clientsLink = new RouterLink("Clientes", ClientView.class);
        RouterLink accountsLink = new RouterLink("Contas", AccountView.class);
        RouterLink balanceLink = new RouterLink("Consultar Saldo", BalanceView.class);
        RouterLink transferLink = new RouterLink("Transferências", TransferView.class);
        RouterLink statementLink = new RouterLink("Extrato", StatementView.class);

        VerticalLayout drawerLayout = new VerticalLayout(
                clientsLink,
                accountsLink,
                balanceLink,
                transferLink,
                statementLink
        );

        drawerLayout.setPadding(true);
        drawerLayout.setSpacing(true);

        addToDrawer(drawerLayout);
    }
}

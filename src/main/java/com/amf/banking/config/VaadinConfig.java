package com.amf.banking.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

@Component
public class VaadinConfig implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        // Permite que URLs da API REST e Swagger não sejam interceptadas pelo Vaadin
        event.getSource().addUIInitListener(initEvent -> {
            initEvent.getUI().addBeforeEnterListener(enterEvent -> {
                String location = enterEvent.getLocation().getPath();

                // Não intercepta rotas da API REST e Swagger
                if (location.startsWith("api/") ||
                    location.startsWith("api-docs") ||
                    location.startsWith("swagger-ui") ||
                    location.startsWith("v3/api-docs")) {
                    // Deixa passar para o Spring MVC
                    return;
                }
            });
        });
    }
}

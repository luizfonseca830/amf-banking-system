package com.amf.banking.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("pt", "BR"));
        return slr;
    }

    @Bean
    public I18NProvider i18NProvider() {
        return new I18NProvider() {
            @Override
            public List<Locale> getProvidedLocales() {
                return Arrays.asList(new Locale("pt", "BR"));
            }

            @Override
            public String getTranslation(String key, Locale locale, Object... params) {
                return key;
            }
        };
    }
}

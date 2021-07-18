package com.funck.webflux.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

@Component
@RequiredArgsConstructor
public class WebTestClientUtil {

    private final ApplicationContext context;

    public WebTestClient authenticateClient(String username, String password) {
        return WebTestClient.bindToApplicationContext(context)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .configureClient()
                .filter(ExchangeFilterFunctions.basicAuthentication(username, password))
                .build();
    }

}

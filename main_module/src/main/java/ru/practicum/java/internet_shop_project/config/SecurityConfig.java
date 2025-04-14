package ru.practicum.java.internet_shop_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public ReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(
            ReactiveClientRegistrationRepository clients,
            ReactiveOAuth2AuthorizedClientService authorizedClients) {

        var provider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var manager = new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clients, authorizedClients);
        manager.setAuthorizedClientProvider(provider);

        return manager;
    }

}

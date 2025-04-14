package ru.practicum.java.internet_shop_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentClientConfigV2 {

    @Value("${spring.payment-service-address}")
    public String paymentServiceAddress;

    @Bean
    public WebClient paymentServiceWebClient(ReactiveOAuth2AuthorizedClientManager manager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(manager);
        oauth2.setDefaultClientRegistrationId("payment-service-client");

        return WebClient.builder()
                .baseUrl(paymentServiceAddress)
                .filter(oauth2)
                .build();
    }

}

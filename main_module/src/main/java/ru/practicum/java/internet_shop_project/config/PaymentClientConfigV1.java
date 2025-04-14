package ru.practicum.java.internet_shop_project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.java.paymentclient.api.DefaultApi;
import ru.practicum.java.paymentclient.invoker.ApiClient;

@Configuration
@Deprecated
public class PaymentClientConfigV1 {

    @Value("${spring.payment-service-address}")
    public String paymentServiceAddress;

    @Bean
    public ApiClient paymentApiClient() {
        return new ApiClient()
                .setBasePath(paymentServiceAddress);
    }

    @Bean
    public DefaultApi paymentDefaultApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }

}

package ru.practicum.java.payment_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity security) throws Exception {
        return security
                .authorizeExchange(requests -> requests
                        .anyExchange().hasAuthority("ROLE_SERVICE")
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2ResourceServer(serverSpec -> serverSpec
                        .jwt(jwtSpec -> {
                            ReactiveJwtAuthenticationConverter jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverter();

                            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
                                String clientId = jwt.getClaimAsString("client_id");
                                System.out.println("Запрос от клиента: " + clientId);
                                if ("main_module_m2m".equals(clientId)) {
                                    return Flux.just(new SimpleGrantedAuthority("ROLE_SERVICE"));
                                } else {
                                    return Flux.just();
                                }
                            });
                            jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter);
                        })
                )
                .build();
    }

}

package ru.practicum.java.internet_shop_project.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.entity.User;
import ru.practicum.java.internet_shop_project.repository.UserRepository;

@Service
public class UserContextService {

    private final UserRepository userRepository;

    public UserContextService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<Long> getCurrentUserId(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            OAuth2User oauth2User = token.getPrincipal();
            String username = (String) oauth2User.getAttributes().get("preferred_username");

            return userRepository.findByUsername(username)
                    .switchIfEmpty(
                            userRepository.save(User.builder()
                                    .username(username)
                                    .role("ROLE_USER")
                                    .build())
                    )
                    .map(User::getId);
        } else {
            return Mono.error(new IllegalStateException("Unsupported authentication type"));
        }
    }

}

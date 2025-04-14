package ru.practicum.java.internet_shop_project.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ViewAccessHelper {
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }
}

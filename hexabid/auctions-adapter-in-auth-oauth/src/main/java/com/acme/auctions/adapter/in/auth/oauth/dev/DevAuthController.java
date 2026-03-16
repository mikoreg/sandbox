package com.acme.auctions.adapter.in.auth.oauth.dev;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Optional;

@Controller
@Profile("dev")
class DevAuthController {

    private final DevUserCatalog devUserCatalog;

    DevAuthController(DevUserCatalog devUserCatalog) {
        this.devUserCatalog = devUserCatalog;
    }

    @GetMapping("/login/dev")
    ModelAndView devLoginPage() {
        return new ModelAndView("dev-login", Map.of("users", devUserCatalog.users()));
    }

    private static Optional<AuthenticatedUser> currentAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() == null
                ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return Optional.of(authenticatedUser);
        }
        return Optional.empty();
    }
}

package com.acme.auctions.adapter.in.auth.oauth.dev;

import com.acme.auctions.auth.core.identityaccess.model.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
@Profile("dev")
class DevAuthController {

    private final DevUserCatalog devUserCatalog;

    DevAuthController(DevUserCatalog devUserCatalog) {
        this.devUserCatalog = devUserCatalog;
    }

    @GetMapping(value = "/dev-auth", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String devAuthPage(HttpServletRequest request) {
        Optional<AuthenticatedUser> currentUser = currentAuthenticatedUser();
        String redirect = safeRedirectTarget(request.getParameter("redirect"));

        StringBuilder html = new StringBuilder();
        html.append("""
                <!doctype html>
                <html lang="pl">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Hexabid Dev Auth</title>
                  <style>
                    body { font-family: system-ui, sans-serif; margin: 0; background: #f4f7fb; color: #0f172a; }
                    main { max-width: 980px; margin: 0 auto; padding: 32px 20px 56px; }
                    .hero, .card { background: white; border: 1px solid rgba(15,23,42,.08); border-radius: 20px; box-shadow: 0 18px 40px rgba(15,23,42,.08); }
                    .hero { padding: 28px; margin-bottom: 18px; }
                    .grid { display: grid; gap: 16px; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); }
                    .card { padding: 18px; }
                    .badge { display: inline-block; padding: 6px 10px; border-radius: 999px; font-size: 12px; font-weight: 700; }
                    .ok { background: rgba(22,163,74,.12); color: #166534; }
                    .off { background: rgba(220,38,38,.10); color: #991b1b; }
                    .actions { display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
                    a.button, button { display: inline-flex; align-items: center; justify-content: center; padding: 11px 14px; border-radius: 12px; text-decoration: none; border: 0; cursor: pointer; font-weight: 700; }
                    a.primary { background: #0b5ed7; color: white; }
                    a.secondary, button { background: #eef3ff; color: #1841b8; }
                    p, li { color: #475569; line-height: 1.5; }
                    code { background: #f1f5f9; padding: 2px 6px; border-radius: 8px; }
                  </style>
                </head>
                <body>
                <main>
                """);

        html.append("<section class='hero'>");
        html.append("<h1>Hexabid Dev Auth</h1>");
        html.append("<p>W tym trybie mozesz przelaczac lokalnego uzytkownika developerskiego bez Google i GitHub.</p>");
        currentUser.ifPresentOrElse(
                user -> html.append("<p>Aktywny uzytkownik: <strong>")
                        .append(user.displayName())
                        .append("</strong> <code>")
                        .append(user.partyId().value())
                        .append("</code></p>"),
                () -> html.append("<p>Brak aktywnego uzytkownika developerskiego.</p>")
        );
        html.append("<div class='actions'>")
                .append("<a class='button secondary' href='")
                .append(redirect)
                .append("'>Powrot do aplikacji</a>")
                .append("<a class='button secondary' href='/logout'>Wyloguj przez Spring Security</a>")
                .append("</div>");
        html.append("</section>");

        html.append("<section class='grid'>");
        for (DevUserCatalog.DevUserEntry user : devUserCatalog.users()) {
            html.append("<article class='card'>")
                    .append("<h2>")
                    .append(user.displayName())
                    .append("</h2>")
                    .append("<p><code>dev:")
                    .append(user.username())
                    .append("</code></p>")
                    .append("<p>")
                    .append(user.description())
                    .append("</p>")
                    .append("<span class='badge ")
                    .append(user.verified() ? "ok'>KYC verified" : "off'>KYC blocked")
                    .append("</span>")
                    .append("<div class='actions'>")
                    .append("<a class='button primary' href='/dev-auth/impersonate?user=")
                    .append(user.username())
                    .append("&redirect=")
                    .append(redirect)
                    .append("'>Zaloguj jako ten user</a>")
                    .append("</div>")
                    .append("</article>");
        }
        html.append("</section></main></body></html>");
        return html.toString();
    }

    @GetMapping("/dev-auth/impersonate")
    String impersonate(
            @RequestParam("user") String username,
            @RequestParam(name = "redirect", required = false) String redirect,
            HttpServletRequest request
    ) {
        DevUserCatalog.DevUserEntry devUser = devUserCatalog.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Unknown dev user: " + username));

        AuthenticatedUser authenticatedUser = devUser.toAuthenticatedUser();
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                authenticatedUser,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return "redirect:" + safeRedirectTarget(redirect);
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

    private static String safeRedirectTarget(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return "http://localhost:4200/";
        }
        if (redirect.startsWith("http://localhost:4200") || redirect.startsWith("/")) {
            return redirect;
        }
        return "http://localhost:4200/";
    }
}

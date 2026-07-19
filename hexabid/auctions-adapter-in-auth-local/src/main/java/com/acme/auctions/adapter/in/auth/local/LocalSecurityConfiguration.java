package com.acme.auctions.adapter.in.auth.local;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Deweloperska wtyczka uwierzytelniania lokalnego.
 *
 * Dostarcza:
 * - InMemoryUserDetailsManager z testowymi użytkownikami
 * - SecurityFilterChain z formLogin (tylko jeśli żaden inny łańcuch nie jest zdefiniowany,
 *   np. gdy adapter auth-oauth NIE jest na classpath)
 *
 * Wzorzec LISTY: ta wtyczka może współistnieć z innymi dostawcami uwierzytelniania
 * (np. OAuth2). Gdy OAuth2 jest obecny, jego SecurityFilterChain ma pierwszeństwo,
 * a ta klasa dostarcza jedynie użytkowników lokalnych.
 */
@Configuration
public class LocalSecurityConfiguration {

    /**
     * SecurityFilterChain z formLogin — aktywowany TYLKO gdy żaden inny
     * SecurityFilterChain nie został zarejestrowany (np. brak adaptera OAuth2).
     */
    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @org.springframework.beans.factory.annotation.Autowired(required = false) com.acme.auctions.adapter.in.auth.oauth.dev.DevOAuth2UserService devOauth2UserService
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/", "/error", "/login", "/login/**", "/logout", "/dev-auth/**").permitAll()
                        .requestMatchers("/h2-console/**", "/ws-auctions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auctions", "/api/auctions/*", "/api/auth/providers").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.defaultSuccessUrl("/", true))
                .oauth2Login(oauth2 -> {
                    oauth2.defaultSuccessUrl("/", true);
                    if (devOauth2UserService != null) {
                        oauth2.userInfoEndpoint(userInfo -> userInfo.userService(devOauth2UserService));
                    }
                })
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager localUserDetailsService() {
        UserDetails user1 = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user1, admin);
    }
}

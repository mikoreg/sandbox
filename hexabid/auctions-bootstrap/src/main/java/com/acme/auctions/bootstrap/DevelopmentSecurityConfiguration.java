package com.acme.auctions.bootstrap;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
class DevelopmentSecurityConfiguration {
}

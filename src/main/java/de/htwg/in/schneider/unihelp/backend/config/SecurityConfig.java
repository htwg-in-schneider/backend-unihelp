package de.htwg.in.schneider.unihelp.backend.config;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/profile").authenticated()
                        .requestMatchers("/api/moderation/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/booking/**").authenticated()
                        .requestMatchers("/api/messages/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/offer", "/api/offer/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/offer/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/offer/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/offer", "/api/offer/**").permitAll()
                        .requestMatchers("/api/**").permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(withDefaults()))
                .build();
    }
}

package com.inu.sts.support_ticket_sla_tracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Basic Auth. AGENT = read + write, VIEWER = read only.
 * Users: agent/agent (AGENT), viewer/viewer (VIEWER).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("AGENT", "VIEWER")
                        .requestMatchers("/api/v1/**").hasRole("AGENT")
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic();
        http.headers(h -> h.frameOptions(f -> f.sameOrigin())); // for H2 console
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var agent = User.builder().username("agent").password(encoder.encode("agent")).roles("AGENT").build();
        var viewer = User.builder().username("viewer").password(encoder.encode("viewer")).roles("VIEWER").build();
        return new InMemoryUserDetailsManager(agent, viewer);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

package com.example.omapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/*
     * ==========================================
     *        HTTP GET /LOGIN REQUEST FLOW
     * ==========================================
     *
     *  Browser
     *     │
     *     ▼
     *  GET /login
     *     │
     *     ▼
     *  SecurityFilterChain (Applies security checks)
     *     │
     *     ▼
     *  AuthController.loginPage() (Processes request)
     *     │
     *     ▼
     *  model.addAttribute(...) (Binds form data object)
     *     │
     *     ▼
     *  return "login" (Returns view name)
     *     │
     *     ▼
     *  ViewResolver (Finds matching template file)
     *     │
     *     ▼
     *  templates/login.html (Renders page UI)
     *     │
     *     ▼
     *  Browser (Displays login page to user)
     * 
     * ==========================================
     */

/**
 * Configuration class for application security settings.
 * Configures password encoding and HTTP authorization boundaries.
 */
@Configuration
public class SecurityConfig {

    /**
     * Provides the BCrypt password encoder bean.
     * Used across the application for secure password hashing and verification.
     *
     * @return a BCrypt-backed PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the main HTTP security filter chain.
     * Defines URL protection rules, disables default form login configurations, 
     * sets up programmatic logout policies, and manages concurrent user sessions.
     *
     * @param http the HttpSecurity builder to configure
     * @return the built SecurityFilterChain
     * @throws Exception if an error occurs during security construction
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure URL access rules based on user roles
            .authorizeHttpRequests(auth -> auth
                // Allow public access to core authentication pages and static assets
                .requestMatchers(
                        "/login",
                        "/signup"
                ).permitAll()
                
                // Restrict customer workflows
                .requestMatchers("/discover/**").hasRole("CUSTOMER")
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                // Restrict merchant workflows
                .requestMatchers("/merchant/**").hasRole("MERCHANT")
                
                // Assets mappin
                
                // Require authentication for any other unmapped request
                .anyRequest().authenticated()
            )
            
            // Disable default login page UI as handling is manual inside controllers
            .formLogin(form -> form.disable())
            
            // Configure custom programmatic logout workflow
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            
            // Manage user session limits
            .sessionManagement(session -> session
                .maximumSessions(1)
            );

        return http.build();
    }
}
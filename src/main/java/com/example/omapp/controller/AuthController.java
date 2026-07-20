package com.example.omapp.controller;

import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.omapp.dto.LoginRequest;
import com.example.omapp.dto.SignupRequest;
import com.example.omapp.security.Role;
import com.example.omapp.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Controller responsible for handling user authentication flows 
 * including rendering forms and processing signups and logins.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Displays the login page.
     * Prepares an empty LoginRequest object in the model to bind form data.
     *
     * @param model the UI model
     * @return the login view template name
     */
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    /**
     * Displays the signup page.
     * Prepares an empty SignupRequest object in the model to bind form data.
     *
     * @param model the UI model
     * UI model used when passing arguments to HTML
     * @return the signup view template name
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    /**
     * Handles the submission of the signup form.
     * Registers a new user via the AuthService and redirects to the login page.
     *
     * @param request the signup data transfer object from the form
     * @return a redirect string to the login page
     */
    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupRequest request, Model model) {
        try {
            authService.register(request);
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("signupRequest", request);

            return "signup";
        }
    }

    /**
     * Handles the submission of the login form.
     * Validates user credentials, programmatically creates a Spring Security session,
     * stores user role attributes in the session, and redirects users to their 
     * respective landing pages based on their Role.
     *
     * @param request     the login credentials from the form
     * @param httpRequest the standard servlet request to access the session
     * @return a redirect string targeting either the customer discovery page or the merchant dashboard
     */
    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest request, HttpServletRequest httpRequest, Model model) {
        
        // Validate credentials and fetch the user's role
        try {
                Role role = authService.login(request);

            
            // Create authenticated token with the granted authority role
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
            );

            // Explicitly set the security context
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            
            // Persist the security context into the HTTP session so it survives across requests
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            // Store role as a raw session attribute for alternative frontend/logic checks
            httpRequest.getSession().setAttribute("role", role.name());
            
            // Redirect logic based on user role mapping
            if (role == Role.CUSTOMER) {
                return "redirect:/discover";
            }
            
            return "redirect:/merchant/dashboard";
        }
        catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loginRequest", request);

            return "login";
        }
    }
}
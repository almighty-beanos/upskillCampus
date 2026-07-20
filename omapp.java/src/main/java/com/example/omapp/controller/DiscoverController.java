package com.example.omapp.controller;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.omapp.entity.Customer;
import com.example.omapp.repository.CustomerRepository;
import com.example.omapp.service.DiscoverService;

@Controller
public class DiscoverController {

    private final DiscoverService discoverService;
    private final CustomerRepository customerRepository;

    public DiscoverController(DiscoverService discoverService, CustomerRepository customerRepository) {
        this.discoverService = discoverService;
        this.customerRepository = customerRepository;
    }

    /**
     * Landing page for the consumer-facing discover feed.
     * NOTE: primary access control (requiring ROLE_CUSTOMER) should live in
     * SecurityConfig via .requestMatchers("/discover").hasRole("CUSTOMER").
     * The check below is just a defensive fallback in case this route is
     * ever reachable without that filter chain rule in place.
     */
    @GetMapping("/discover")
    public String discover(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        
        // 1. Run your query (this returns the Optional wrapper you saw)
        Optional<Customer> customerOptional = customerRepository.findByEmail(authentication.getName());

        String customerName = customerOptional
            .orElseThrow(() -> new RuntimeException("Customer not found with email"))
            .getCustomerName();

        model.addAttribute("customerName", customerName);
        model.addAttribute("goodsList", discoverService.getAllGoods());
        model.addAttribute("servicesList", discoverService.getAllServices());

        return "discover";
    }

    
}
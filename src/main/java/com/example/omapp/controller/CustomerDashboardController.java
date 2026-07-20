package com.example.omapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.omapp.dto.ProfileUpdate;
import com.example.omapp.entity.Customer;
import com.example.omapp.repository.CustomerRepository;
import com.example.omapp.service.CustomerDashboardService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer/dashboard")
public class CustomerDashboardController {

    private final CustomerDashboardService dashboardService;
    private final CustomerRepository customerRepository;

    // Explicit constructor injection
    public CustomerDashboardController(CustomerDashboardService dashboardService, CustomerRepository customerRepository) {
        this.dashboardService = dashboardService;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        Integer customerId = getCurrentCustomerId();
        model.addAttribute("profile", dashboardService.getProfile(customerId));
        model.addAttribute("transactions", dashboardService.getPurchaseHistory(customerId));
        model.addAttribute("appointments", dashboardService.getAppointmentHistory(customerId));
        model.addAttribute("updateDto", new ProfileUpdate()); // Matches ProfileUpdate.java in your DTO package
        return "customerdashboard";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("updateDto") @Valid ProfileUpdate dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "customerdashboard"; 
        }

        try {
            Integer customerId = getCurrentCustomerId();
            dashboardService.updateProfile(customerId, dto);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/dashboard";
    }

    private Integer getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String currentEmail = authentication.getName();  // This is usually the username/email used during login

        // Fetch customer by email
        return customerRepository.findByEmail(currentEmail)
                .map(Customer::getCustomerId)
                .orElseThrow(() -> new RuntimeException("Customer not found for email: " + currentEmail));
    }
}
package com.example.omapp.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.omapp.dto.UpdateMerchantProfileRequest;
import com.example.omapp.entity.Merchant;
import com.example.omapp.service.MerchantDashboardService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/merchant")
public class MerchantDashboardController {

    private final MerchantDashboardService merchantDashboardService;

    public MerchantDashboardController(MerchantDashboardService merchantDashboardService) {
        this.merchantDashboardService = merchantDashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());

        model.addAttribute("profile", merchantDashboardService.getProfile(merchant));
        model.addAttribute("transactions", merchantDashboardService.getTransactions(merchant.getMerchantId()));
        model.addAttribute("appointments", merchantDashboardService.getAppointments(merchant.getMerchantId()));

        if (!model.containsAttribute("updateRequest")) {
            model.addAttribute("updateRequest", new UpdateMerchantProfileRequest());
        }

        return "merchantdashboard";
    }

    @PostMapping("/dashboard/profile")
    public String updateProfile(Authentication authentication,
                                 @ModelAttribute("updateRequest") UpdateMerchantProfileRequest request,
                                 HttpServletRequest httpRequest,
                                 RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
            merchantDashboardService.updateProfile(merchant, request);

            // AuthController uses the merchant's email as the session principal (see
            // UsernamePasswordAuthenticationToken(request.getEmail(), ...) at login). If the
            // email just changed, refresh the SecurityContext the same way so the *next*
            // request still resolves this merchant instead of looking up a stale email.
            if (merchant.getEmail() != null && !merchant.getEmail().equals(authentication.getName())) {
                Authentication refreshed = new UsernamePasswordAuthenticationToken(
                        merchant.getEmail(), null, authentication.getAuthorities());

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(refreshed);
                SecurityContextHolder.setContext(context);

                httpRequest.getSession(true).setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            }

            redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("profileError", e.getMessage());
        }

        return "redirect:/merchant/dashboard";
    }
}
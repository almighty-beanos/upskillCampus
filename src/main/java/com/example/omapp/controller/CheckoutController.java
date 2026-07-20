package com.example.omapp.controller;

import com.example.omapp.dto.AppointmentBookingRequest;
import com.example.omapp.dto.GoodsPurchaseRequest;
import com.example.omapp.service.CheckoutService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // ---------- goods ----------

    @GetMapping("/goods/{id}")
    public String goodsCheckout(Authentication authentication, @PathVariable Integer id, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("goods", checkoutService.getGoodsForCheckout(id));
        } catch (RuntimeException e) {
            return "redirect:/discover";
        }

        if (!model.containsAttribute("purchaseRequest")) {
            model.addAttribute("purchaseRequest", new GoodsPurchaseRequest(1));
        }

        return "checkout-goods";
    }

    @PostMapping("/goods/{id}")
    public String purchaseGoods(Authentication authentication, @PathVariable Integer id,
                                 @ModelAttribute("purchaseRequest") GoodsPurchaseRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            Integer transactionId = checkoutService.purchaseGoods(authentication.getName(), id, request);
            return "redirect:/checkout/thank-you/transaction/" + transactionId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("checkoutError", e.getMessage());
            redirectAttributes.addFlashAttribute("purchaseRequest", request);
            return "redirect:/checkout/goods/" + id;
        }
    }

    // ---------- services ----------

    @GetMapping("/services/{id}")
    public String serviceCheckout(Authentication authentication, @PathVariable Integer id, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("service", checkoutService.getServiceForCheckout(id));
        } catch (RuntimeException e) {
            return "redirect:/discover";
        }

        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", new AppointmentBookingRequest());
        }

        return "checkout-services";
    }

    @PostMapping("/services/{id}")
    public String bookService(Authentication authentication, @PathVariable Integer id,
                               @ModelAttribute("bookingRequest") AppointmentBookingRequest request,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            Integer appointmentId = checkoutService.bookAppointment(authentication.getName(), id, request);
            return "redirect:/checkout/thank-you/appointment/" + appointmentId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("checkoutError", e.getMessage());
            redirectAttributes.addFlashAttribute("bookingRequest", request);
            return "redirect:/checkout/services/" + id;
        }
    }

    // ---------- thank you ----------

    @GetMapping("/thank-you/transaction/{id}")
    public String thankYouTransaction(Authentication authentication, @PathVariable Integer id, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("thankYou", checkoutService.getTransactionThankYou(id, authentication.getName()));
        return "thank-you";
    }

    @GetMapping("/thank-you/appointment/{id}")
    public String thankYouAppointment(Authentication authentication, @PathVariable Integer id, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("thankYou", checkoutService.getAppointmentThankYou(id, authentication.getName()));
        return "thank-you";
    }
}
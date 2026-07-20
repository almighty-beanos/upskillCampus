package com.example.omapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.omapp.dto.GoodsForm;
import com.example.omapp.dto.ServiceForm;
import com.example.omapp.entity.Merchant;
import com.example.omapp.service.CatalogService;
import com.example.omapp.service.MerchantDashboardService;

@Controller
@RequestMapping("/merchant/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    private final MerchantDashboardService merchantDashboardService;

    public CatalogController(CatalogService catalogService, MerchantDashboardService merchantDashboardService) {
        this.catalogService = catalogService;
        this.merchantDashboardService = merchantDashboardService;
    }

    // ---------- listing + add ----------

    @GetMapping
    public String catalog(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());

        model.addAttribute("goodsList", catalogService.listGoods(merchant.getMerchantId()));
        model.addAttribute("servicesList", catalogService.listServices(merchant.getMerchantId()));

        if (!model.containsAttribute("goodsForm")) {
            model.addAttribute("goodsForm", new GoodsForm());
        }
        if (!model.containsAttribute("serviceForm")) {
            model.addAttribute("serviceForm", new ServiceForm());
        }

        return "catalog";
    }

    @PostMapping("/goods")
    public String createGoods(Authentication authentication,
                               @ModelAttribute("goodsForm") GoodsForm form,
                               RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            Integer goodsId = catalogService.createGoods(merchant, form);
            redirectAttributes.addFlashAttribute("editSuccess", "Listing created — add some photos below.");
            return "redirect:/merchant/catalog/goods/" + goodsId + "/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("catalogError", e.getMessage());
            return "redirect:/merchant/catalog";
        }
    }

    @PostMapping("/services")
    public String createService(Authentication authentication,
                                 @ModelAttribute("serviceForm") ServiceForm form,
                                 RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            Integer serviceId = catalogService.createService(merchant, form);
            redirectAttributes.addFlashAttribute("editSuccess", "Listing created — add some photos below.");
            return "redirect:/merchant/catalog/services/" + serviceId + "/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("catalogError", e.getMessage());
            return "redirect:/merchant/catalog";
        }
    }

    // ---------- goods: edit + media ----------

    @GetMapping("/goods/{id}/edit")
    public String editGoods(Authentication authentication, @PathVariable Integer id, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        model.addAttribute("goods", catalogService.getGoodsForEdit(merchant.getMerchantId(), id));
        return "catalog-goods-edit";
    }

    @PostMapping("/goods/{id}")
    public String updateGoods(Authentication authentication, @PathVariable Integer id,
                               @ModelAttribute GoodsForm form, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            catalogService.updateGoods(merchant.getMerchantId(), id, form);
            redirectAttributes.addFlashAttribute("editSuccess", "Changes saved.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("editError", e.getMessage());
        }
        return "redirect:/merchant/catalog/goods/" + id + "/edit";
    }

    @PostMapping("/goods/{id}/media")
    public String uploadGoodsMedia(Authentication authentication, @PathVariable Integer id,
                                    @RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Please choose a file first");
            }
            catalogService.addGoodsMedia(merchant.getMerchantId(), id, file);
            redirectAttributes.addFlashAttribute("editSuccess", "Media uploaded.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("editError", e.getMessage());
        }
        return "redirect:/merchant/catalog/goods/" + id + "/edit";
    }

    @PostMapping("/goods/{id}/media/delete")
    public String deleteGoodsMedia(Authentication authentication, @PathVariable Integer id,
                                    @RequestParam("mediaUrl") String mediaUrl,
                                    RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            catalogService.deleteGoodsMedia(merchant.getMerchantId(), id, mediaUrl);
            redirectAttributes.addFlashAttribute("editSuccess", "Media removed.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("editError", e.getMessage());
        }
        return "redirect:/merchant/catalog/goods/" + id + "/edit";
    }

    // ---------- services: edit + media ----------

    @GetMapping("/services/{id}/edit")
    public String editService(Authentication authentication, @PathVariable Integer id, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        model.addAttribute("service", catalogService.getServiceForEdit(merchant.getMerchantId(), id));
        return "catalog-services-edit";
    }

    @PostMapping("/services/{id}")
    public String updateService(Authentication authentication, @PathVariable Integer id,
                                 @ModelAttribute ServiceForm form, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            catalogService.updateService(merchant.getMerchantId(), id, form);
            redirectAttributes.addFlashAttribute("editSuccess", "Changes saved.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("editError", e.getMessage());
        }
        return "redirect:/merchant/catalog/services/" + id + "/edit";
    }

    @PostMapping("/services/{id}/media")
    public String uploadServiceMedia(Authentication authentication, @PathVariable Integer id,
                                      @RequestParam("file") MultipartFile file,
                                      RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Please choose a file first");
            }
            catalogService.addServiceMedia(merchant.getMerchantId(), id, file);
            redirectAttributes.addFlashAttribute("editSuccess", "Media uploaded.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("editError", e.getMessage());
        }
        return "redirect:/merchant/catalog/services/" + id + "/edit";
    }

    @PostMapping("/services/{id}/media/delete")
    public String deleteServiceMedia(Authentication authentication, @PathVariable Integer id,
                                      @RequestParam("mediaUrl") String mediaUrl,
                                      RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Merchant merchant = merchantDashboardService.getMerchantByEmail(authentication.getName());
        try {
            catalogService.deleteServiceMedia(merchant.getMerchantId(), id, mediaUrl);
            redirectAttributes.addFlashAttribute("editSuccess", "Media removed.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("editError", e.getMessage());
        }
        return "redirect:/merchant/catalog/services/" + id + "/edit";
    }
}
package com.example.omapp.service;

import com.example.omapp.dto.CatalogGoods;
import com.example.omapp.dto.CatalogServiceDTO;
import com.example.omapp.dto.GoodsForm;
import com.example.omapp.dto.ServiceForm;
import com.example.omapp.entity.Goods;
import com.example.omapp.entity.Merchant;
import com.example.omapp.entity.ServiceEntity;
import com.example.omapp.repository.GoodsRepository;
import com.example.omapp.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final GoodsRepository goodsRepository;
    private final ServiceRepository serviceRepository;
    private final SupabaseStorageService storageService;

    public CatalogService(GoodsRepository goodsRepository,
                           ServiceRepository serviceRepository,
                           SupabaseStorageService storageService) {
        this.goodsRepository = goodsRepository;
        this.serviceRepository = serviceRepository;
        this.storageService = storageService;
    }

    public List<CatalogGoods> listGoods(Integer merchantId) {
        return goodsRepository.findByMerchant_MerchantId(merchantId).stream()
                .map(this::toGoodsDto)
                .collect(Collectors.toList());
    }

    public List<CatalogServiceDTO> listServices(Integer merchantId) {
        return serviceRepository.findByMerchant_MerchantId(merchantId).stream()
                .map(this::toServiceDto)
                .collect(Collectors.toList());
    }

    public CatalogGoods getGoodsForEdit(Integer merchantId, Integer goodsId) {
        return toGoodsDto(loadOwnedGoods(merchantId, goodsId));
    }

    public CatalogServiceDTO getServiceForEdit(Integer merchantId, Integer serviceId) {
        return toServiceDto(loadOwnedService(merchantId, serviceId));
    }

    @Transactional
    public Integer createGoods(Merchant merchant, GoodsForm form) {
        Goods goods = new Goods();
        goods.setMerchant(merchant);
        goods.setMediaUrls(new String[0]);
        applyGoodsForm(goods, form);
        return goodsRepository.save(goods).getGoodsId();
    }

    @Transactional
    public Integer createService(Merchant merchant, ServiceForm form) {
        ServiceEntity service = new ServiceEntity();
        service.setMerchant(merchant);
        service.setMediaUrls(new String[0]);
        applyServiceForm(service, form);
        return serviceRepository.save(service).getServiceId();
    }

    @Transactional
    public void updateGoods(Integer merchantId, Integer goodsId, GoodsForm form) {
        Goods goods = loadOwnedGoods(merchantId, goodsId);
        applyGoodsForm(goods, form);
        goodsRepository.save(goods);
    }

    @Transactional
    public void updateService(Integer merchantId, Integer serviceId, ServiceForm form) {
        ServiceEntity service = loadOwnedService(merchantId, serviceId);
        applyServiceForm(service, form);
        serviceRepository.save(service);
    }

    @Transactional
    public void addGoodsMedia(Integer merchantId, Integer goodsId, MultipartFile file) {
        Goods goods = loadOwnedGoods(merchantId, goodsId);
        String url = storageService.uploadFile(file, merchantId, goodsId);
        goods.setMediaUrls(appendUrl(goods.getMediaUrls(), url));
        goodsRepository.save(goods);
    }

    @Transactional
    public void addServiceMedia(Integer merchantId, Integer serviceId, MultipartFile file) {
        ServiceEntity service = loadOwnedService(merchantId, serviceId);
        String url = storageService.uploadFile(file, merchantId, serviceId);
        service.setMediaUrls(appendUrl(service.getMediaUrls(), url));
        serviceRepository.save(service);
    }

    @Transactional
    public void deleteGoodsMedia(Integer merchantId, Integer goodsId, String mediaUrl) {
        Goods goods = loadOwnedGoods(merchantId, goodsId);
        storageService.deleteFile(mediaUrl);
        goods.setMediaUrls(removeUrl(goods.getMediaUrls(), mediaUrl));
        goodsRepository.save(goods);
    }

    @Transactional
    public void deleteServiceMedia(Integer merchantId, Integer serviceId, String mediaUrl) {
        ServiceEntity service = loadOwnedService(merchantId, serviceId);
        storageService.deleteFile(mediaUrl);
        service.setMediaUrls(removeUrl(service.getMediaUrls(), mediaUrl));
        serviceRepository.save(service);
    }

    // ---------- ownership-checked loaders ----------

    private Goods loadOwnedGoods(Integer merchantId, Integer goodsId) {
        Goods goods = goodsRepository.findByIdWithMerchant(goodsId)
                .orElseThrow(() -> new RuntimeException("Goods listing not found"));
        if (!goods.getMerchant().getMerchantId().equals(merchantId)) {
            throw new RuntimeException("You do not have permission to modify this listing");
        }
        return goods;
    }

    private ServiceEntity loadOwnedService(Integer merchantId, Integer serviceId) {
        ServiceEntity service = serviceRepository.findByIdWithMerchant(serviceId)
                .orElseThrow(() -> new RuntimeException("Service listing not found"));
        if (!service.getMerchant().getMerchantId().equals(merchantId)) {
            throw new RuntimeException("You do not have permission to modify this listing");
        }
        return service;
    }

    // ---------- mapping helpers ----------

    private void applyGoodsForm(Goods goods, GoodsForm form) {
        goods.setGoodsName(form.getGoodsName());
        goods.setGoodsDesc(form.getGoodsDesc());
        goods.setGoodsCategory(form.getGoodsCategory());
        goods.setPrice(form.getPrice());
    }

    private void applyServiceForm(ServiceEntity service, ServiceForm form) {
        service.setServiceCategory(form.getServiceCategory());
        service.setServiceDesc(form.getServiceDesc());
        service.setPricePerHour(form.getPricePerHour());
        service.setAvailabilityStartTime(form.getAvailabilityStartTime());
        service.setAvailabilityEndTime(form.getAvailabilityEndTime());
    }

    private String[] appendUrl(String[] existing, String url) {
        List<String> list = existing == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(existing));
        list.add(url);
        return list.toArray(new String[0]);
    }

    private String[] removeUrl(String[] existing, String url) {
        if (existing == null) {
            return new String[0];
        }
        return Arrays.stream(existing).filter(u -> !u.equals(url)).toArray(String[]::new);
    }

    private CatalogGoods toGoodsDto(Goods goods) {
        return CatalogGoods.builder()
                .goodsId(goods.getGoodsId())
                .goodsName(goods.getGoodsName())
                .goodsDesc(goods.getGoodsDesc())
                .goodsCategory(goods.getGoodsCategory())
                .price(goods.getPrice())
                .mediaUrls(goods.getMediaUrls() == null ? Collections.emptyList() : Arrays.asList(goods.getMediaUrls()))
                .build();
    }

    private CatalogServiceDTO toServiceDto(ServiceEntity service) {
        return CatalogServiceDTO.builder()
                .serviceId(service.getServiceId())
                .serviceCategory(service.getServiceCategory())
                .serviceDesc(service.getServiceDesc())
                .pricePerHour(service.getPricePerHour())
                .availabilityStartTime(service.getAvailabilityStartTime())
                .availabilityEndTime(service.getAvailabilityEndTime())
                .mediaUrls(service.getMediaUrls() == null ? Collections.emptyList() : Arrays.asList(service.getMediaUrls()))
                .build();
    }
}

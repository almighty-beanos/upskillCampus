package com.example.omapp.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.omapp.dto.GoodsDiscover;
import com.example.omapp.dto.ServiceDiscover;
import com.example.omapp.entity.Goods;
import com.example.omapp.entity.ServiceEntity;
import com.example.omapp.repository.GoodsRepository;
import com.example.omapp.repository.ServiceRepository;

@Service
public class DiscoverService {

    // Served from src/main/resources/assets/NoPhoto.png via WebConfig's resource handler.
    private static final String DEFAULT_PHOTO = "/assets/NoPhoto.png";

    private final GoodsRepository goodsRepository;
    private final ServiceRepository serviceRepository;

    public DiscoverService(GoodsRepository goodsRepository, ServiceRepository serviceRepository) {
        this.goodsRepository = goodsRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<GoodsDiscover> getAllGoods() {
        return goodsRepository.findAll().stream()
                .map(this::toGoodsDto)
                .collect(Collectors.toList());
    }

    public List<ServiceDiscover> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::toServiceDto)
                .collect(Collectors.toList());
    }

    private GoodsDiscover toGoodsDto(Goods goods) {
        return GoodsDiscover.builder()
                .goodsId(goods.getGoodsId())
                .goodsName(goods.getGoodsName())
                .goodsDesc(goods.getGoodsDesc())
                .goodsCategory(goods.getGoodsCategory())
                .price(goods.getPrice())
                .merchantName(goods.getMerchant().getMerchantName())
                .mediaUrls(resolveMedia(goods.getMediaUrls()))
                .build();
    }

    private ServiceDiscover toServiceDto(ServiceEntity service) {
        return ServiceDiscover.builder()
                .serviceId(service.getServiceId())
                .serviceCategory(service.getServiceCategory())
                .serviceDesc(service.getServiceDesc())
                .pricePerHour(service.getPricePerHour())
                .availabilityStartTime(service.getAvailabilityStartTime())
                .availabilityEndTime(service.getAvailabilityEndTime())
                .merchantName(service.getMerchant().getMerchantName())
                .mediaUrls(resolveMedia(service.getMediaUrls()))
                .build();
    }

    /**
     * mediaUrls contains full public Supabase URLs.
     */
    private List<String> resolveMedia(String[] mediaUrls) {
        if (mediaUrls == null || mediaUrls.length == 0) {
            return Collections.singletonList(DEFAULT_PHOTO);
        }
        List<String> urls = Arrays.stream(mediaUrls)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList());
        return urls.isEmpty() ? Collections.singletonList(DEFAULT_PHOTO) : urls;
    }
}
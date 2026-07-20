package com.example.omapp.dto;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceDiscover {

    private Integer serviceId;
    private String serviceCategory;
    private String serviceDesc;
    private BigDecimal pricePerHour;
    private LocalTime availabilityStartTime;
    private LocalTime availabilityEndTime;
    private String merchantName;
    private List<String> mediaUrls;
}
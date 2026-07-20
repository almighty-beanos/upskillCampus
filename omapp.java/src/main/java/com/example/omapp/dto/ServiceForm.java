package com.example.omapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceForm {

    private String serviceCategory;
    private String serviceDesc;
    private BigDecimal pricePerHour;
    private LocalTime availabilityStartTime;
    private LocalTime availabilityEndTime;
}
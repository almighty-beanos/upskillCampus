package com.example.omapp.dto;

import java.math.BigDecimal;

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
public class ThankYouDto {

    private String type;

    private Integer referenceId;
    private String itemName;
    private BigDecimal totalAmount;
    private String merchantName;
    private String merchantEmail;
    private String merchantPhone;
}
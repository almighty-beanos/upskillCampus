package com.example.omapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantProfile {

    private Integer merchantId;
    private String merchantName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private BigDecimal walletBalance;
}
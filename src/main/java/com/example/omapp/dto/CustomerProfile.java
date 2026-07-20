package com.example.omapp.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfile {
    private Integer customerId;
    private String customerName;
    private String email;
    private String phone;
    private BigDecimal walletBalance;
}
package com.example.omapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutGoods {

    private Integer goodsId;
    private String goodsName;
    private String goodsDesc;
    private String goodsCategory;
    private BigDecimal price;
    private String merchantName;
    private String mediaUrl;
}
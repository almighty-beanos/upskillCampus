package com.example.omapp.dto;

import java.math.BigDecimal;
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
public class GoodsDiscover {

    private Integer goodsId;
    private String goodsName;
    private String goodsDesc;
    private String goodsCategory;
    private BigDecimal price;
    private String merchantName;
    private List<String> mediaUrls;
}
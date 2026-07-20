package com.example.omapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogGoods {

    private Integer goodsId;
    private String goodsName;
    private String goodsDesc;
    private String goodsCategory;
    private BigDecimal price;
    private List<String> mediaUrls;
}
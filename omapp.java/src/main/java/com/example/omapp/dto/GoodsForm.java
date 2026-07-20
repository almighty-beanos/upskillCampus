package com.example.omapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GoodsForm {

    private String goodsName;
    private String goodsDesc;
    private String goodsCategory;
    private BigDecimal price;
}
package com.example.omapp.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name="goods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Goods {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer goodsId;


    private String goodsName;


    @Column(columnDefinition="TEXT")
    private String goodsDesc;


    private String goodsCategory;


    @ManyToOne
    @JoinColumn(name="merchant_id")
    private Merchant merchant;


    private BigDecimal price;


    @Column(columnDefinition="text[]")
    private String[] mediaUrls;
}
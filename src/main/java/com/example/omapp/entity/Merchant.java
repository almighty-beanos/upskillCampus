// Merchant Entity used to map to the relational database relation directly (Merchants.sql)
// Removes possibility of writing SQL queries and closing / opening connections
package com.example.omapp.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="merchants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer merchantId;

    private String merchantName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String phone;

    private LocalDateTime createdAt = LocalDateTime.now();

    private BigDecimal walletBalance = BigDecimal.ZERO;


    @OneToMany(mappedBy = "merchant")
    private List<ServiceEntity> services;


    @OneToMany(mappedBy = "merchant")
    private List<Goods> goods;
}
package com.example.omapp.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

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
@Table(name="services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer serviceId;


    private String serviceCategory;

    @Column(columnDefinition="TEXT")
    private String serviceDesc;


    @ManyToOne
    @JoinColumn(name="merchant_id")
    private Merchant merchant;


    private LocalTime availabilityStartTime;

    private LocalTime availabilityEndTime;

    private BigDecimal pricePerHour;


    @Column(columnDefinition="text[]")
    private String[] mediaUrls;
}
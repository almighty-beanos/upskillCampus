package com.example.omapp.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
@Table(name="appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer appointmentId;


    @ManyToOne
    @JoinColumn(name="service_id")
    private ServiceEntity service;


    @ManyToOne
    @JoinColumn(name="merchant_id")
    private Merchant merchant;


    @ManyToOne
    @JoinColumn(name="customer_id")
    private Customer customer;


    private LocalDate appointmentStartDate;


    private LocalDate appointmentEndDate;


    private LocalTime startingTime;


    private Integer workingHours;


    private LocalDateTime createdAt = LocalDateTime.now();
}
package com.example.omapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
public class AppointmentSales {
    private Integer appointmentId;
    private String serviceCategory;
    private String customerName;
    private String merchantName;
    private LocalDate appointmentStartDate;
    private LocalDate appointmentEndDate;
    private LocalTime startingTime;
    private Integer workingHours;
    private BigDecimal pricePerHour;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
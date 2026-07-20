package com.example.omapp.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentBookingRequest {

    private LocalDate appointmentStartDate;
    private LocalDate appointmentEndDate;
    private LocalTime startingTime;
    private Integer workingHours;
}
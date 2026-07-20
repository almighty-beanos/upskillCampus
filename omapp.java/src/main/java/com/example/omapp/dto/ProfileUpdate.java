package com.example.omapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdate {
    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    private String currentPassword; // required for any update

    private String newPassword; // optional
}
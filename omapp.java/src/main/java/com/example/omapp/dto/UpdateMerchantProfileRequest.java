package com.example.omapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMerchantProfileRequest {

    private String merchantName;
    private String email;
    private String phone;

    /** Required for any change — verified against the stored hash before anything is saved. */
    private String currentPassword;

    /** Optional — only set/validated if the merchant is also changing their password. */
    private String newPassword;
    private String confirmNewPassword;
}
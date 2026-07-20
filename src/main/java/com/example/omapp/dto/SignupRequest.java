package com.example.omapp.dto;

import com.example.omapp.security.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String name;

    private String email;

    private String phone;

    private String password;

    private Role role;
}
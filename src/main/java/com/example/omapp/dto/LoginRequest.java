package com.example.omapp.dto;

import com.example.omapp.security.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    private String email;

    private String password;

    private Role role;
}
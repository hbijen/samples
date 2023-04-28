package com.example.authserver.model;

import com.example.authserver.enums.RegisterWith;

import lombok.Data;

@Data
public class SignupModel {
    private String email;
    private String phone;
    private String password;
    private String repassword;
    private String verifyCode;
    private RegisterWith registerWith;
}

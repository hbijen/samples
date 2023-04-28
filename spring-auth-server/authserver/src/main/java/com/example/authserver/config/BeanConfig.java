package com.example.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;

import com.example.authserver.validator.SignupModelValidator;

@Configuration
public class BeanConfig {
    
    @Bean("signupValidator")
    public Validator signupValidator() {
        return new SignupModelValidator();
    }
}

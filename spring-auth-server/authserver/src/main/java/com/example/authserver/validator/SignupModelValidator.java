package com.example.authserver.validator;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.example.authserver.enums.RegisterWith;
import com.example.authserver.model.SignupModel;

public class SignupModelValidator implements Validator {

    // ref: https://www.baeldung.com/java-email-validation-regex#regular-expression-by-rfc-5322-for-email-validation
    final private String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    @Override
    public boolean supports(Class<?> clazz) {
        return SignupModel.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignupModel signupModel = (SignupModel) target;

        
        if (signupModel.getRegisterWith() == RegisterWith.email) {

            if (!StringUtils.hasLength(signupModel.getEmail())) {
                errors.rejectValue("email", "email.empty");
            }
            if (!signupModel.getEmail().matches(emailRegex)) {
                errors.rejectValue("email", "email.invalid");
            }
            if (signupModel.getPassword().length() < 6 ) {
                errors.rejectValue("password", "password.min");
            }
            if (!signupModel.getPassword().equals(signupModel.getRepassword())) {
                errors.rejectValue("repassword", "password.mismatch");
            }
        } else if (signupModel.getRegisterWith() == RegisterWith.sms) {
            if (!StringUtils.hasLength(signupModel.getPhone())) {
                errors.rejectValue("phone", "phone.empty");
            }
            if (!signupModel.getPhone().matches("^\\+\\d+")) {
                errors.rejectValue("phone", "phone.invalid");
            }
        }
    }
    
}

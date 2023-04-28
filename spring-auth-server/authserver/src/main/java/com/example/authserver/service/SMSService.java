package com.example.authserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

@Service
public class SMSService {
    
    @Value("${twilio.account}")
    String ACCOUNT_SID;
    
    @Value("${twilio.token}")
    String AUTH_TOKEN;
    
    @Value("${twilio.phone}")
    String TWILIO_PHONE;
    
    @Value("${twilio.enabled}")
    Boolean TWILIO_ENABLED;

    @PostConstruct
    public void init() {
        if (TWILIO_ENABLED) {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        }
    }

    public String sendOtp(String toPhoneNo, String text) {

        Message message = Message.creator(
                                    new PhoneNumber(toPhoneNo),
                                    new PhoneNumber(TWILIO_PHONE),
                                    text
                                ).create();
        return message.getSid();
    }
}

package com.example.authserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;


    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        messageHelper.setTo(to);
        messageHelper.setSubject(subject);
        messageHelper.setText(htmlContent, true);
        mailSender.send(mimeMessage);
    }

    public void sendEmailVerifyCode(String to, String name, String code) throws MessagingException {

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("code", code);
        String htmlContent = templateEngine.process("verify-email-template", context);

        sendEmail(to, "Verify Email", htmlContent);
    }
}

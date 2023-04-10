package io.renren.modules.tokenatm.service.impl;

import io.renren.modules.tokenatm.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component("EmailService")
public class EmailServiceImpl implements EmailService {


    @Override
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(text);
    }
}
package org.xyz.luckyjourney.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.xyz.luckyjourney.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private SimpleMailMessage simpleMailMessage;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromName;

    @Override
    public void send(String email, String text) {
        simpleMailMessage.setSubject("幸运日");
        simpleMailMessage.setFrom(fromName);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setText(text);
        javaMailSender.send(simpleMailMessage);
    }
}

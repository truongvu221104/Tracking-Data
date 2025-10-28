package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendVerificationEmail(String to, String verifyUrl) {
        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Xác thực tài khoản của bạn");
        msg.setText("""
        Chào bạn,

        Vui lòng nhấn vào liên kết sau để xác thực email:
        %s

        Nếu bạn không yêu cầu, hãy bỏ qua email này.
        """.formatted(verifyUrl));
        mailSender.send(msg);
    }
}

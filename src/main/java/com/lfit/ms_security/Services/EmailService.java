package com.lfit.ms_security.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendConfirmationEmail(String toEmail, String userName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Confirmación de registro - LFit");
        message.setText(
                "Hola " + userName + ",\n\n" +
                        "Tu cuenta ha sido creada exitosamente.\n\n" +
                        "Bienvenido a LFit 💪\n\n" +
                        "Saludos,\nEl equipo de LFit"
        );
        mailSender.send(message);
    }
}
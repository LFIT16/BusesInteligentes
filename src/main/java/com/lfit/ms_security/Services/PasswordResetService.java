package com.lfit.ms_security.Services;

import com.lfit.ms_security.Models.Session;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Repositories.SessionRepository;
import com.lfit.ms_security.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private SessionRepository theSessionRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EncryptionService theEncryptionService;

    @Value("${recaptcha.secret.v3}")
    private String recaptchaSecretV3;

    @Value("${recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String mailFrom;

    // Verificar reCAPTCHA v3
    public boolean verifyRecaptchaV3(String token) {
        RestTemplate rest = new RestTemplate();
        String url = recaptchaVerifyUrl
                + "?secret=" + recaptchaSecretV3
                + "&response=" + token;
        Map response = rest.postForObject(url, null, Map.class);
        if (response == null) return false;
        boolean success = (Boolean) response.get("success");
        // v3 retorna una puntuación — 0.5 o más es humano
        Double score = (Double) response.get("score");
        return success && score != null && score >= 0.5f;
    }

    // Solicitar recuperación de contraseña
    public void requestPasswordReset(String email) {
        User user = theUserRepository.getUserByEmail(email);

        // Si no existe simplemente no hace nada
        // El mensaje genérico se envía desde el controlador
        if (user == null) return;

        // Generar token único válido por 30 minutos
        String token = UUID.randomUUID().toString();
        Date expiration = new Date(System.currentTimeMillis() + 30 * 60 * 1000);

        // Guardar en Session con type PASSWORD_RESET
        Session session = new Session(token, expiration, null);
        session.setUser(user);
        session.setType("PASSWORD_RESET");
        theSessionRepository.save(session);

        // Enviar email con enlace
        String resetLink = frontendUrl + "/#/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Recuperación de contraseña");
        message.setText("Haz clic en el siguiente enlace para recuperar tu contraseña:\n\n"
                + resetLink + "\n\nEste enlace expira en 30 minutos.\n\n"
                + "Si no solicitaste este cambio, ignora este mensaje.");
        mailSender.send(message);
    }

    // Resetear contraseña con token
    public boolean resetPassword(String token, String newPassword) {
        Session session = theSessionRepository.findByToken(token).orElse(null);

        if (session == null) return false;
        if (!"PASSWORD_RESET".equals(session.getType())) return false;
        if (session.getExpiration().before(new Date())) return false;

        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";
        if (!newPassword.matches(passwordRegex)) return false;

        // Actualizar contraseña
        User user = session.getUser();
        user.setPassword(theEncryptionService.convertSHA256(newPassword));
        theUserRepository.save(user);

        theSessionRepository.delete(session);

        return true;
    }
}
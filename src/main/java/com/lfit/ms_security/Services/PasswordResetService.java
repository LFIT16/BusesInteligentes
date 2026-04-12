package com.lfit.ms_security.Services;

import com.lfit.ms_security.Models.Session;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Repositories.SessionRepository;
import com.lfit.ms_security.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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
    private EncryptionService theEncryptionService;

    @Autowired
    private EmailService theEmailService;

    @Value("${recaptcha.secret.v3}")
    private String recaptchaSecretV3;

    @Value("${recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public boolean verifyRecaptchaV3(String token) {
        try {
            RestTemplate rest = new RestTemplate();
            String url = recaptchaVerifyUrl
                    + "?secret=" + recaptchaSecretV3
                    + "&response=" + token;

            Map response = rest.postForObject(url, null, Map.class);
            if (response == null) return false;

            boolean success = Boolean.TRUE.equals(response.get("success"));
            Double score = response.get("score") instanceof Number
                    ? ((Number) response.get("score")).doubleValue()
                    : null;

            return success && score != null && score >= 0.5;
        } catch (Exception e) {
            System.out.println("Error verificando reCAPTCHA: " + e.getMessage());
            return false;
        }
    }

    public void requestPasswordReset(String email) {
        User user = theUserRepository.getUserByEmail(email);

        if (user == null) return;

        String token = UUID.randomUUID().toString();
        Date expiration = new Date(System.currentTimeMillis() + 30 * 60 * 1000);

        Session session = new Session(token, expiration, null);
        session.setUser(user);
        session.setType("PASSWORD_RESET");
        theSessionRepository.save(session);

        String resetLink = frontendUrl + "/#/reset-password?token=" + token;
        this.theEmailService.sendPasswordResetEmail(email, resetLink);
    }

    public boolean resetPassword(String token, String newPassword) {
        Session session = theSessionRepository.findByToken(token).orElse(null);

        if (session == null) return false;
        if (!"PASSWORD_RESET".equals(session.getType())) return false;
        if (session.getExpiration().before(new Date())) return false;

        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";
        if (!newPassword.matches(passwordRegex)) return false;

        User user = session.getUser();
        user.setPassword(theEncryptionService.convertSHA256(newPassword));
        theUserRepository.save(user);

        theSessionRepository.delete(session);

        return true;
    }
}
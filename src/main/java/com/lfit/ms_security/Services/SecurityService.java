package com.lfit.ms_security.Services;
import com.lfit.ms_security.Models.Session;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Repositories.SessionRepository;
import com.lfit.ms_security.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;


@Service
public class SecurityService {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private EncryptionService theEncryptionService;
    @Autowired
    private JwtService theJwtService;
    @Autowired
    private JavaMailSender mailSender;

    public String login(User theNewUser){
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());
        if(theActualUser != null &&
                theActualUser.getPassword().equals(
                        theEncryptionService.convertSHA256(theNewUser.getPassword()))) {
            return theJwtService.generateToken(theActualUser);
        }
        return null;
    }

    public User getUserByEmail(String email){
        return this.theUserRepository.getUserByEmail(email);
    }
    /*
    public boolean permissionsValidation(final HttpServletRequest request,
                                         @RequestBody Permission thePermission) {
        boolean success=this.theValidatorsService.validationRolePermission(request,thePermission.getUrl(),thePermission.getMethod());
        return success;
    }
    */
    private String generate2FACode() {
        int number = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(number);
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return "***@***.com";

        String local = parts[0];

        String localMasked = local.length() >= 2
                ? local.substring(0, 2) + "***"
                : local.charAt(0) + "***";

        return localMasked + "@***.com";
    }

    private void sendTwoFactorCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Código de verificación 2FA");
        message.setText(
                "Su código de verificación es: " + code + "\n\n" +
                        "Este código expira en 5 minutos."
        );
        mailSender.send(message);
    }

    public HashMap<String, Object> startTwoFactorLogin(User loginUser) {
        HashMap<String, Object> response = new HashMap<>();

        String token = this.login(loginUser);
        if (token == null) return null;

        User user = this.getUserByEmail(loginUser.getEmail());

        String code = generate2FACode();
        Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

        Session session = new Session(null, expiration, code);
        session.setUser(user);
        session.setType("TWO_FACTOR");
        session.setAttempts(0);
        session.setActive(true);

        theSessionRepository.save(session);

        sendTwoFactorCode(user.getEmail(), code);

        response.put("requires2fa", true);
        response.put("challengeId", session.getId());
        response.put("maskedEmail", maskEmail(user.getEmail()));
        response.put("expiresInSeconds", 300);
        response.put("remainingAttempts", 3);

        return response;
    }
    public HashMap<String, Object> verifyTwoFactorCode(String challengeId, String code) {
        HashMap<String, Object> response = new HashMap<>();

        Session session = theSessionRepository.findById(challengeId).orElse(null);

        if (session == null || !"TWO_FACTOR".equals(session.getType()) || Boolean.FALSE.equals(session.getActive())) {
            response.put("success", false);
            response.put("message", "Sesión inválida. Debe volver a iniciar sesión.");
            return response;
        }

        if (session.getExpiration().before(new Date())) {
            session.setActive(false);
            theSessionRepository.save(session);

            response.put("success", false);
            response.put("message", "El código expiró. Debe volver a iniciar sesión.");
            return response;
        }

        if (!session.getCode2FA().equals(code)) {
            int attempts = session.getAttempts() == null ? 0 : session.getAttempts();
            attempts++;
            session.setAttempts(attempts);

            if (attempts >= 3) {
                session.setActive(false);
                theSessionRepository.save(session);

                response.put("success", false);
                response.put("message", "Sesión inválida. Debe volver a iniciar sesión.");
                response.put("remainingAttempts", 0);
                return response;
            }

            theSessionRepository.save(session);

            response.put("success", false);
            response.put("message", "Código incorrecto. Intentos restantes: " + (3 - attempts));
            response.put("remainingAttempts", 3 - attempts);
            return response;
        }

        User user = session.getUser();
        String finalToken = theJwtService.generateToken(user);

        session.setActive(false);
        session.setType("AUTH");
        session.setToken(finalToken);
        theSessionRepository.save(session);

        response.put("success", true);
        response.put("token", finalToken);
        response.put("user", user);

        return response;
    }
    public HashMap<String, Object> resendTwoFactorCode(String challengeId) {
        HashMap<String, Object> response = new HashMap<>();

        Session session = theSessionRepository.findById(challengeId).orElse(null);

        if (session == null || !"TWO_FACTOR".equals(session.getType()) || Boolean.FALSE.equals(session.getActive())) {
            response.put("success", false);
            response.put("message", "Sesión inválida.");
            return response;
        }

        if (session.getExpiration().before(new Date())) {
            session.setActive(false);
            theSessionRepository.save(session);

            response.put("success", false);
            response.put("message", "La sesión expiró. Debe volver a iniciar sesión.");
            return response;
        }

        String newCode = generate2FACode();
        Date newExpiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

        session.setCode2FA(newCode);
        session.setExpiration(newExpiration);
        session.setAttempts(0);

        theSessionRepository.save(session);

        sendTwoFactorCode(session.getUser().getEmail(), newCode);

        response.put("success", true);
        response.put("expiresInSeconds", 300);
        response.put("remainingAttempts", 3);

        return response;
    }
    public void cancelTwoFactorSession(String challengeId) {
        Session session = theSessionRepository.findById(challengeId).orElse(null);

        if (session == null) return;

        if ("TWO_FACTOR".equals(session.getType()) && Boolean.TRUE.equals(session.getActive())) {
            session.setActive(false);
            theSessionRepository.save(session);
        }
    }

}





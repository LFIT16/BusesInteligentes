package com.lfit.ms_security.Services;

<<<<<<< Updated upstream
=======
import com.lfit.ms_security.Models.Permission;
import com.lfit.ms_security.Models.Role;
import com.lfit.ms_security.Models.User;
import org.springframework.beans.factory.annotation.Autowired;
>>>>>>> Stashed changes
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

<<<<<<< Updated upstream
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
=======
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public void sendPasswordResetEmail(String email, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Recuperación de contraseña");
        message.setText(
                "Haz clic en el siguiente enlace para recuperar tu contraseña:\n\n" +
                        resetLink + "\n\n" +
                        "Este enlace expira en 30 minutos.\n\n" +
                        "Si no solicitaste este cambio, ignora este mensaje."
        );
        mailSender.send(message);
    }

    public void sendTwoFactorCodeEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Código de verificación 2FA");
        message.setText(
                "Su código de verificación es: " + code + "\n\n" +
                        "Este código expira en 5 minutos."
        );
        mailSender.send(message);
    }

    public void sendRolesChangedEmail(User user, List<Role> oldRoles, List<Role> newRoles) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String oldRolesText = formatRoles(oldRoles);
        String newRolesText = formatRoles(newRoles);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Cambio de roles en su cuenta");
        message.setText(
                "Hola " + user.getName() + ",\n\n" +
                        "Le informamos que sus roles fueron actualizados.\n\n" +
                        "Roles anteriores: " + oldRolesText + "\n" +
                        "Roles actuales: " + newRolesText + "\n\n" +
                        "Si usted no reconoce este cambio, contacte al administrador."
        );
        mailSender.send(message);
    }

    public void sendPermissionsChangedEmail(User user, String roleName,
                                            List<Permission> oldPermissions,
                                            List<Permission> newPermissions) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String oldPermissionsText = formatPermissions(oldPermissions);
        String newPermissionsText = formatPermissions(newPermissions);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Cambio de permisos en su cuenta");
        message.setText(
                "Hola " + user.getName() + ",\n\n" +
                        "Le informamos que cambiaron los permisos asociados a su rol " + roleName + ".\n\n" +
                        "Permisos anteriores: " + oldPermissionsText + "\n" +
                        "Permisos actuales: " + newPermissionsText + "\n\n" +
                        "Si usted no reconoce este cambio, contacte al administrador."
        );
        mailSender.send(message);
    }

    private String formatRoles(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "Sin roles";
        }

        return roles.stream()
                .map(Role::getName)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String formatPermissions(List<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return "Sin permisos";
        }

        return permissions.stream()
                .map(permission -> permission.getUrl() + " [" + permission.getMethod() + "]")
                .distinct()
                .collect(Collectors.joining(", "));
    }
>>>>>>> Stashed changes
}
package com.lfit.ms_security.Controllers;

import com.lfit.ms_security.Services.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    @Autowired
    private PasswordResetService thePasswordResetService;

    // Solicitar recuperación
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(
            @RequestParam String email,
            @RequestParam String recaptchaTokenV3
    ) {
        // Verificar reCAPTCHA
        if (!thePasswordResetService.verifyRecaptchaV3(recaptchaTokenV3)) {
            return ResponseEntity.badRequest().body("reCAPTCHA inválido.");
        }

        thePasswordResetService.requestPasswordReset(email);

        // Mensaje genérico por seguridad
        return ResponseEntity.ok("Si el email existe, recibirá instrucciones de recuperación.");
    }

    // Resetear contraseña
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {
        boolean success = thePasswordResetService.resetPassword(token, newPassword);
        if (success) {
            return ResponseEntity.ok("Contraseña actualizada correctamente.");
        }
        return ResponseEntity.badRequest().body("Token inválido o expirado.");
    }
}
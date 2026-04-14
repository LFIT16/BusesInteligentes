package com.lfit.ms_security.Controllers;

import com.lfit.ms_security.Services.PasswordResetService;
import com.lfit.ms_security.Services.RecaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/public/auth/password")
@CrossOrigin(origins = "http://localhost:4200")
public class PasswordResetController {

    @Autowired
    private PasswordResetService thePasswordResetService;

    @Autowired
    private RecaptchaService recaptchaService; // ← añade esto

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(
            @RequestParam String email,
            @RequestParam String recaptchaTokenV3
    ) {
        if (!recaptchaService.verifyRecaptchaV3(recaptchaTokenV3)) { // ← cambia esto
            return ResponseEntity.badRequest().body("reCAPTCHA inválido.");
        }
        thePasswordResetService.requestPasswordReset(email);
        return ResponseEntity.ok("Si el email existe, recibirá instrucciones de recuperación.");
    }

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
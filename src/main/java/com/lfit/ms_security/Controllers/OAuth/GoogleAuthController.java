package com.lfit.ms_security.Controllers.OAuth;

import com.lfit.ms_security.Services.OAuth.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/auth/google")
@CrossOrigin(origins = "http://localhost:4200")
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService theGoogleAuthService;

    @Value("${google.frontend-url}")
    private String frontendUrl;

    @GetMapping("/url")
    public Map<String, String> getGoogleUrl() {
        return Map.of("url", theGoogleAuthService.getGoogleUrl());
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        try {
            String accessToken = theGoogleAuthService.getGoogleAccessToken(code);
            Map googleUser = theGoogleAuthService.getGoogleUser(accessToken);
            String token = theGoogleAuthService.processGoogleUser(googleUser);

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/#/auth/google/success?token=" + token)
                    .build();
        } catch (Exception e) {
            System.err.println("Error en callback de Google: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/#/login")
                    .build();
        }
    }
}
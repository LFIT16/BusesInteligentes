package com.lfit.ms_security.Controllers.OAuth;

import com.lfit.ms_security.Services.OAuth.MicrosoftAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/microsoft")
@CrossOrigin(origins = "http://localhost:4200")
public class MicrosoftAuthController {

    @Autowired
    private MicrosoftAuthService theMicrosoftAuthService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @GetMapping("/url")
    public Map<String, String> getMicrosoftUrl() {
        return Map.of("url", theMicrosoftAuthService.getMicrosoftUrl());
    }

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        try {
            String accessToken = theMicrosoftAuthService.getMicrosoftAccessToken(code);
            Map microsoftUser = theMicrosoftAuthService.getMicrosoftUser(accessToken);
            String result = theMicrosoftAuthService.processMicrosoftUser(microsoftUser, accessToken);;

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/#/auth/microsoft/success?token=" + result)
                    .build();

        } catch (Exception e) {
            System.err.println("Error en callback Microsoft: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/#/login")
                    .build();
        }
    }
}
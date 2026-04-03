package com.lfit.ms_security.Controllers.OAuth;

import com.lfit.ms_security.Services.OAuth.GitHubAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth/github")
@CrossOrigin(origins = "http://localhost:4200")
public class GitHubAuthController {
    @Autowired
    private GitHubAuthService theGithubAuthService;

    @Value("${github.frontend-url}")
    private String frontendUrl;

    @GetMapping("/url")
    public Map<String, String> getGithubUrl() {
        return Map.of("url", theGithubAuthService.getGithubUrl());
    }
    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        try {
            String accessToken = theGithubAuthService.getGithubAccessToken(code);
            Map githubUser = theGithubAuthService.getGithubUser(accessToken);
            String result = theGithubAuthService.processGithubUser(githubUser, accessToken);

            if (result.startsWith("NEEDS_EMAIL:")) {
                String data = result.replace("NEEDS_EMAIL:", "");
                String redirectUrl = frontendUrl + "/#/auth/github/email-required?data=" + data;
                System.out.println("Redirigiendo a: " + redirectUrl);
                return ResponseEntity
                        .status(HttpStatus.FOUND)
                        .header("Location", redirectUrl)
                        .build();
            }

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/#/auth/github/success?token=" + result)
                    .build();
        } catch (Exception e) {
            System.err.println("Error en callback: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", frontendUrl + "/#/login")
                    .build();
        }
    }
    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeRegistration(
            @RequestParam String githubUsername,
            @RequestParam String photo,
            @RequestParam String name,
            @RequestParam String email
    ) {
        String jwt = theGithubAuthService.createOrLinkUser(email, name, photo, githubUsername);
        return ResponseEntity.ok(Map.of("token", jwt));
    }
}

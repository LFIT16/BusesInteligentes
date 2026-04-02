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
public class GitHubAuthControlle {
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
        String accessToken = theGithubAuthService.getGithubAccessToken(code);
        Map githubUser = theGithubAuthService.getGithubUser(accessToken);
        String jwt = theGithubAuthService.processGithubUser(githubUser);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header("Location", frontendUrl + "/auth/github/success?token=" + jwt)
                .build();
    }
}

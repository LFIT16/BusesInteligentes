package com.lfit.ms_security.Services.OAuth;

import com.lfit.ms_security.Models.Profile;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Repositories.ProfileRepository;
import com.lfit.ms_security.Repositories.UserRepository;
import com.lfit.ms_security.Services.EncryptionService;
import com.lfit.ms_security.Services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GitHubAuthService {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private ProfileRepository theProfileRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    @Autowired
    private JwtService theJwtService;

    // Generar URL de GitHub
    public String getGithubUrl() {
        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&scope=user:email read:user";
    }

    // Intercambiar code por access token
    public String getGithubAccessToken(String code) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> body = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code
        );

        ResponseEntity<Map> response = rest.postForEntity(
                "https://github.com/login/oauth/access_token",
                new HttpEntity<>(body, headers),
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    // Obtener datos del usuario de GitHub
    public Map getGithubUser(String accessToken) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Map> response = rest.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        return response.getBody();
    }

    public String getGithubEmail(String accessToken) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<List> response = rest.exchange(
                "https://api.github.com/user/emails",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        List<Map> emails = response.getBody();
        for (Map emailData : emails) {
            if ((Boolean) emailData.get("primary")) {
                return (String) emailData.get("email");
            }
        }
        return null;
    }

    // Procesar usuario
    public String processGithubUser(Map githubUser, String accessToken) {
        String email = (String) githubUser.get("email");
        String name = (String) githubUser.get("name");
        String photo = (String) githubUser.get("avatar_url");
        String githubUsername = (String) githubUser.get("login");

        if (name == null || name.isEmpty()) {
            name = githubUsername;
        }

        // Buscar primero por githubUsername — ya se registró antes
        User existingUser = theUserRepository.findByGithubUsername(githubUsername).orElse(null);
        if (existingUser != null) {
            // Ya existe — generar token directamente sin pedir email
            return theJwtService.generateToken(existingUser);
        }

        // Si email directo es null y no existe por githubUsername → primera vez
        if (email == null || email.isEmpty()) {
            return "NEEDS_EMAIL:" + githubUsername + "|" + photo + "|" + name;
        }

        return createOrLinkUser(email, name, photo, githubUsername);
    }

    // creación del usuario
    public String createOrLinkUser(String email, String name, String photo, String githubUsername) {
        User user = theUserRepository.getUserByEmail(email);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGithubUsername(githubUsername);
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(theEncryptionService.convertSHA256(randomPassword));
            theUserRepository.save(user);

            Profile profile = new Profile();
            profile.setPhoto(photo);
            profile.setUser(user);
            theProfileRepository.save(profile);
        } else {
            user.setGithubUsername(githubUsername);
            theUserRepository.save(user);
        }

        return theJwtService.generateToken(user);
    }
}
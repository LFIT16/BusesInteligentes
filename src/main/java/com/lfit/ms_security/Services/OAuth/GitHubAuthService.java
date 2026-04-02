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

    @Value("${github.frontend-url}")
    private String frontendUrl;

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private ProfileRepository theProfileRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    @Autowired
    private JwtService theJwtService;

    // ✅ Generar URL de GitHub
    public String getGithubUrl() {
        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&scope=user:email read:user";
    }

    // ✅ Intercambiar code por access token
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

    // ✅ Obtener datos del usuario de GitHub
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

    // ✅ Procesar usuario — crear o vincular
    public String processGithubUser(Map githubUser) {
        System.out.println("GitHub user data: " + githubUser);
        String email = (String) githubUser.get("email");
        String name = (String) githubUser.get("name");
        String photo = (String) githubUser.get("avatar_url");
        String githubUsername = (String) githubUser.get("login");

        System.out.println("email: " + email);        // ✅
        System.out.println("name: " + name);          // ✅
        System.out.println("githubUsername: " + githubUsername);

        User user = theUserRepository.getUserByEmail(email);

        if (user == null) {
            // ✅ Primera vez — crear User
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setGithubUsername(githubUsername);
            // ✅ Password aleatorio para evitar inconsistencias
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(theEncryptionService.convertSHA256(randomPassword));
            theUserRepository.save(user);

            // ✅ Crear Profile asociado
            Profile profile = new Profile();
            profile.setPhoto(photo);
            profile.setUser(user);
            theProfileRepository.save(profile);

        } else {
            // ✅ Ya existe — vincular GitHub
            user.setGithubUsername(githubUsername);
            theUserRepository.save(user);
        }

        return theJwtService.generateToken(user);
    }
}
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.frontend-url}")
    private String frontendUrl;

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private ProfileRepository theProfileRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    @Autowired
    private JwtService theJwtService;

    // Generar URL de Google
    public String getGoogleUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=http://localhost:8080/api/public/auth/google/callback"
                + "&response_type=code"
                + "&scope=openid email profile";
    }

    // Intercambiar code por access token
    public String getGoogleAccessToken(String code) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", "http://localhost:8080/auth/google/callback");

        ResponseEntity<Map> response = rest.postForEntity(
                "https://oauth2.googleapis.com/token",
                new HttpEntity<>(body, headers),
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    // Obtener datos del usuario de Google
    public Map getGoogleUser(String accessToken) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Map> response = rest.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        return response.getBody();
    }

    // Procesar usuario
    public String processGoogleUser(Map googleUser) {
        String email = (String) googleUser.get("email");
        String name = (String) googleUser.get("name");
        String photoUrl = (String) googleUser.get("picture");
        String photo = getGooglePhoto(photoUrl);



        return createOrLinkUser(email, name, photo);
    }

    // Crear o vincular usuario
    public String createOrLinkUser(String email, String name, String photo) {
        User user = theUserRepository.getUserByEmail(email);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(theEncryptionService.convertSHA256(randomPassword));
            theUserRepository.save(user);

            Profile profile = new Profile();
            profile.setPhoto(photo);
            profile.setUser(user);
            theProfileRepository.save(profile);
        } else {

            Profile profile = theProfileRepository.findByUser(user).orElse(null);

            if (profile == null) {
                profile = new Profile();
                profile.setUser(user);
            }

            if (photo != null && !photo.isEmpty()) {
                profile.setPhoto(photo);
            }

            theProfileRepository.save(profile);
        }

        return theJwtService.generateToken(user);
    }

    public String getGooglePhoto(String photoUrl) {
        try {
            if (photoUrl == null || photoUrl.isEmpty()) {
                return null;
            }

            RestTemplate rest = new RestTemplate();

            ResponseEntity<byte[]> response = rest.exchange(
                    photoUrl,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            if (response.getBody() != null) {
                String base64 = java.util.Base64.getEncoder().encodeToString(response.getBody());
                return "data:image/jpeg;base64," + base64;
            }
        } catch (Exception e) {
            System.out.println("No fue posible obtener la foto de Google");
        }

        return null;
    }
}
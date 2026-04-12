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

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class MicrosoftAuthService {

    @Value("${microsoft.client-id}")
    private String clientId;

    @Value("${microsoft.client-secret}")
    private String clientSecret;

    @Value("${microsoft.tenant-id}")
    private String tenantId;

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private ProfileRepository theProfileRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    @Autowired
    private JwtService theJwtService;

    private final String redirectUri = "http://localhost:8080/api/public/auth/microsoft/callback";

    // Generar URL de Microsoft
    public String getMicrosoftUrl() {
        return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&response_mode=query"
                + "&scope=openid profile email User.Read"
                + "&prompt=select_account";
    }

    // Intercambiar code por access token
    public String getMicrosoftAccessToken(String code) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");
        body.add("scope", "openid profile email User.Read");

        ResponseEntity<Map> response = rest.postForEntity(
                "https://login.microsoftonline.com/common/oauth2/v2.0/token",
                new HttpEntity<>(body, headers),
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    // Obtener datos del usuario de Microsoft
    public Map getMicrosoftUser(String accessToken) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = rest.exchange(
                "https://graph.microsoft.com/v1.0/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        return response.getBody();
    }

    // Obtener foto del usuario de Microsoft
    public String getMicrosoftPhoto(String accessToken) {
        try {
            RestTemplate rest = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<byte[]> response = rest.exchange(
                    "https://graph.microsoft.com/v1.0/me/photo/$value",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    byte[].class
            );

            if (response.getBody() != null) {
                String base64 = Base64.getEncoder().encodeToString(response.getBody());
                return "data:image/jpeg;base64," + base64;
            }
        } catch (Exception e) {
            System.out.println("El usuario no tiene foto en Microsoft");
        }

        return null;
    }

    // Procesar usuario
    public String processMicrosoftUser(Map microsoftUser, String accessToken) {
        String email = (String) microsoftUser.get("mail");
        if (email == null || email.isEmpty()) {
            email = (String) microsoftUser.get("userPrincipalName");
        }

        String name = (String) microsoftUser.get("displayName");
        if (name == null || name.isEmpty()) {
            name = email;
        }

        String photo = getMicrosoftPhoto(accessToken);

        return createOrLinkUser(email, name, photo);
    }

    // creación o actualización del usuario
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
}
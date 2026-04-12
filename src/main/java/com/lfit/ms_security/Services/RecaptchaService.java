package com.lfit.ms_security.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${recaptcha.secret.v3}")
    private String recaptchaSecretV3;

    @Value("${recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    public boolean verifyRecaptchaV3(String token) {
        RestTemplate rest = new RestTemplate();
        String url = recaptchaVerifyUrl
                + "?secret=" + recaptchaSecretV3
                + "&response=" + token;
        Map response = rest.postForObject(url, null, Map.class);
        if (response == null) return false;
        boolean success = (Boolean) response.get("success");
        Double score = (Double) response.get("score");
        return success && score != null && score >= 0.5;
    }
}
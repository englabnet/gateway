package net.englab.gateway.services;

import lombok.RequiredArgsConstructor;
import net.englab.gateway.models.RecaptchaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RecaptchaService {
    @Value("${gateway.recaptcha.secret-key}")
    private String secretKey;
    @Value("${gateway.recaptcha.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate;

    public RecaptchaResponse validateToken(String recaptchaToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var map = new LinkedMultiValueMap<>();
        map.add("secret", secretKey);
        map.add("response", recaptchaToken);

        var entity = new HttpEntity<>(map, headers);
        var response = restTemplate.exchange(verifyUrl, HttpMethod.POST, entity, RecaptchaResponse.class);

        return response.getBody();
    }

}

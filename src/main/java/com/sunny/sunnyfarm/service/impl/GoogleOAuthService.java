package com.sunny.sunnyfarm.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Google 인증 코드를 사용하여 사용자 이메일 가져오기
    public String getUserEmail(String code) {
        // Access Token 요청
        String accessToken = getAccessToken(code);

        // Access Token으로 사용자 이메일 요청
        try {
            String response = restTemplate.getForObject(
                    userInfoUri + "?access_token=" + accessToken,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("email").asText();
        } catch (Exception e) {
            throw new RuntimeException("사용자 이메일 요청 실패: " + e.getMessage(), e);
        }
    }

    // Google 인증 코드를 사용하여 Access Token 요청
    private String getAccessToken(String code) {
        // 요청 파라미터 설정
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("code", code);
        requestParams.put("client_id", clientId);
        requestParams.put("client_secret", clientSecret);
        requestParams.put("redirect_uri", redirectUri);
        requestParams.put("grant_type", "authorization_code");

        // Google에 POST 요청 보내기
        String response = restTemplate.postForObject(tokenUri, requestParams, String.class);

        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Access Token 요청 실패: " + e.getMessage(), e);
        }
    }
}
